package com.phelti.phelti.service;

import com.phelti.phelti.dto.ActivityRequest;
import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.ActivityAlreadyExistsException;
import com.phelti.phelti.exception.ActivityNotFoundException;
import com.phelti.phelti.exception.InvalidActivityDataException;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidCheckInCodeException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.model.ProposalStatus;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.model.PublicationType;
import com.phelti.phelti.repository.jpa.ActivityJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.phelti.phelti.model.BusinessRules;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int MAX_LOCATION_LENGTH = 200;
    private static final int MAX_VIRTUAL_LINK_LENGTH = 300;

    private final ActivityJpaRepository activityRepository;
    private final UserJpaRepository userRepository;
    private final ProposalService proposalService;
    private final PublicationService publicationService;
    private final WalletService walletService;
    private final com.phelti.phelti.repository.jpa.ConfirmationJpaRepository confirmationRepository;

    public ActivityServiceImpl(
            ActivityJpaRepository activityRepository,
            UserJpaRepository userRepository,
            ProposalService proposalService,
            PublicationService publicationService,
            WalletService walletService, com.phelti.phelti.repository.jpa.ConfirmationJpaRepository confirmationRepository) {

        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.proposalService = proposalService;
        this.publicationService = publicationService;
        this.walletService = walletService;
        this.confirmationRepository = confirmationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateDraft(ActivityRequest request) {
        validateBasicData(request);
        var proposal = proposalService.findProposalById(request.proposalId());
        if (!ProposalStatus.ACCEPTED.name().equals(proposal.getProposalStatus())) {
            throw new InvalidActivityStateException("Solo se pueden acordar horarios de propuestas aceptadas.");
        }
        validateModeData(request, publicationService.findPublicationById(proposal.getPublicationId()).getInteractionMode());
    }

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityRequest request) {

        validateBasicData(request);

        Proposal proposal =
                proposalService.findProposalById(request.proposalId());

        publicationService.lockPublication(proposal.getPublicationId());
        proposal = proposalService.findProposalById(request.proposalId());
        if (!ProposalStatus.ACCEPTED.name().equals(proposal.getProposalStatus())) {
            throw new InvalidActivityStateException(
                    "Solo se pueden programar propuestas aceptadas."
            );
        }

        if (activityRepository.existsByProposalId(proposal.getProposalId())) {
            throw new ActivityAlreadyExistsException(proposal.getProposalId());
        }

        Publication publication =
                publicationService.findPublicationById(proposal.getPublicationId());

        validateModeData(request, publication.getInteractionMode());

        boolean offersSupport = PublicationType.OFFERS_SUPPORT.name()
                .equals(publication.getPublicationType());

        Integer requesterId = offersSupport
                ? proposal.getProposingUserId()
                : publication.getUserId();

        Integer participantId = offersSupport
                ? publication.getUserId()
                : proposal.getProposingUserId();

        userRepository.lockParticipants(java.util.List.of(requesterId, participantId));
        UserEntity requester = findUser(requesterId);
        UserEntity participant = findUser(participantId);
        LocalDateTime start = LocalDateTime.of(request.activityDate(), request.activityTime().withSecond(0).withNano(0));
        LocalDateTime end = start.plusMinutes(request.durationMinutes());
        validateSchedule(requester, start, end);
        validateSchedule(participant, start, end);

        BigDecimal agreedAmount = proposal.getProposedAmount() != null
                ? proposal.getProposedAmount()
                : publication.getPheltAmount();

        // La transferencia requiere que ambos participantes tengan billetera.
        walletService.findWalletByUserId(participantId);

        ActivityEntity activity = new ActivityEntity();
        activity.setPublicationId(publication.getPublicationId());
        activity.setProposalId(proposal.getProposalId());
        activity.setRequester(requester);
        activity.setParticipant(participant);
        activity.setInteractionMode(publication.getInteractionMode());
        activity.setActivityDate(request.activityDate());
        activity.setActivityTime(request.activityTime().withSecond(0).withNano(0));
        activity.setDurationMinutes(request.durationMinutes());
        activity.setLocation(InteractionMode.PRESENTIAL.name().equals(publication.getInteractionMode()) ? request.location().trim() : null);
        activity.setVirtualLink(InteractionMode.VIRTUAL.name().equals(publication.getInteractionMode()) ? request.virtualLink().trim() : null);
        activity.setAgreedAmount(agreedAmount);
        activity.setCheckInCode(String.format(java.util.Locale.ROOT, "%06d", new java.security.SecureRandom().nextInt(1_000_000)));
        activity.setActivityStatus(ActivityStatus.SCHEDULED.name());

        ActivityEntity saved = activityRepository.save(activity);

        walletService.reserveBalance(requesterId, agreedAmount, saved.getActivityId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityEntity findActivityById(Integer activityId) {

        return activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(activityId));
    }

    @Override
    @Transactional
    public ActivityEntity lockActivity(Integer activityId) {
        ActivityEntity activity = activityRepository.findForUpdate(activityId).orElseThrow(() -> new ActivityNotFoundException(activityId));
        org.hibernate.Hibernate.initialize(activity.getRequester());
        org.hibernate.Hibernate.initialize(activity.getParticipant());
        return activity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntity> findActivitiesByUserId(Integer userId) {

        findUser(userId);

        return activityRepository.findByParticipantOrRequesterId(userId);
    }

    @Override
    @Transactional
    public ActivityEntity completeActivity(Integer activityId, String checkInCode) {

        ActivityEntity activity = lockActivity(activityId);

        ensureScheduled(activity, "completar");

        if (checkInCode == null || !checkInCode.equals(activity.getCheckInCode())) {
            throw new InvalidCheckInCodeException("El código de confirmación no coincide.");
        }

        walletService.transferReservedBalance(
                activity.getRequester().getUserId(),
                activity.getParticipant().getUserId(),
                activity.getAgreedAmount(),
                activity.getActivityId()
        );

        publicationService.changePublicationStatus(
                activity.getPublicationId(),
                PublicationStatus.COMPLETED.name()
        );

        activity.setActivityStatus(ActivityStatus.COMPLETED.name());

        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public ActivityEntity cancelActivity(Integer activityId) {

        ActivityEntity activity = lockActivity(activityId);

        ensureScheduled(activity, "cancelar");
        if (confirmationRepository.countByActivityActivityId(activityId) > 0) throw new InvalidActivityStateException("No puedes cancelar unilateralmente una actividad con una confirmación registrada.");

        walletService.releaseReservedBalance(
                activity.getRequester().getUserId(),
                activity.getAgreedAmount(),
                activity.getActivityId()
        );

        proposalService.cancelAcceptedProposal(activity.getProposalId());

        publicationService.changePublicationStatus(
                activity.getPublicationId(),
                PublicationStatus.PUBLISHED.name()
        );

        activity.setActivityStatus(ActivityStatus.CANCELLED.name());

        return activityRepository.save(activity);
    }

    private UserEntity findUser(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void ensureScheduled(ActivityEntity activity, String action) {

        if (!ActivityStatus.SCHEDULED.name().equals(activity.getActivityStatus())) {
            throw new InvalidActivityStateException(
                    "Solo se puede " + action + " una actividad programada."
            );
        }
    }

    private void validateBasicData(ActivityRequest request) {

        if (request.proposalId() == null) {
            throw new InvalidActivityDataException("Selecciona una propuesta.");
        }

        if (request.activityDate() == null) {
            throw new InvalidActivityDataException("Indica la fecha de la actividad.");
        }

        if (request.activityTime() == null) {
            throw new InvalidActivityDataException("Indica la hora de la actividad.");
        }

        if (!LocalDateTime.of(request.activityDate(), request.activityTime().withSecond(0).withNano(0)).isAfter(BusinessRules.now())) {
            throw new InvalidActivityDataException("La actividad debe programarse para una fecha y hora futuras.");
        }
        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new InvalidActivityDataException(
                    "La duración debe ser de al menos un minuto."
            );
        }
    }

    private void validateSchedule(UserEntity user, LocalDateTime start, LocalDateTime end) {
        // Sin horarios registrados, no hay restricciones de disponibilidad declaradas.
        var availability = user.getAvailabilities();
        if (!availability.isEmpty()) {
            boolean fits = start.toLocalDate().equals(end.toLocalDate()) && availability.stream().anyMatch(slot ->
                    start.getDayOfWeek().name().equals(slot.getDayOfWeek()) &&
                    !start.toLocalTime().isBefore(slot.getStartTime()) && !end.toLocalTime().isAfter(slot.getEndTime()));
            if (!fits) throw new InvalidActivityDataException("El horario no coincide con la disponibilidad de ambos participantes.");
        }
        boolean overlaps = activityRepository.findByParticipantOrRequesterId(user.getUserId()).stream()
                .filter(a -> ActivityStatus.SCHEDULED.name().equals(a.getActivityStatus()))
                .anyMatch(a -> {
                    LocalDateTime otherStart = LocalDateTime.of(a.getActivityDate(), a.getActivityTime());
                    LocalDateTime otherEnd = otherStart.plusMinutes(a.getDurationMinutes());
                    return start.isBefore(otherEnd) && otherStart.isBefore(end);
                });
        if (overlaps) throw new InvalidActivityDataException("Uno de los participantes ya tiene otra actividad en ese horario.");
    }

    private void validateModeData(ActivityRequest request, String interactionMode) {

        if (InteractionMode.PRESENTIAL.name().equals(interactionMode)) {
            requireText(request.location(), "El lugar del encuentro", MAX_LOCATION_LENGTH);
        }

        if (InteractionMode.VIRTUAL.name().equals(interactionMode)) {
            requireText(request.virtualLink(), "El enlace de reunión", MAX_VIRTUAL_LINK_LENGTH);
            try {
                java.net.URI link = java.net.URI.create(request.virtualLink().trim());
                if (!"https".equalsIgnoreCase(link.getScheme()) || link.getHost() == null || link.getUserInfo() != null) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new InvalidActivityDataException("Introduce un enlace de reunión HTTPS válido.");
            }
        }
    }

    private void requireText(String value, String field, int maxLength) {

        if (value == null || value.isBlank()) {
            throw new InvalidActivityDataException(
                    field + " es obligatorio para esta modalidad."
            );
        }

        if (value.length() > maxLength) {
            throw new InvalidActivityDataException(
                    field + " admite hasta " + maxLength + " caracteres."
            );
        }
    }
}
