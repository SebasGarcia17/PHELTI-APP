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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int MAX_LOCATION_LENGTH = 200;
    private static final int MAX_VIRTUAL_LINK_LENGTH = 300;

    private final ActivityJpaRepository activityRepository;
    private final UserJpaRepository userRepository;
    private final ProposalService proposalService;
    private final PublicationService publicationService;
    private final WalletService walletService;

    public ActivityServiceImpl(
            ActivityJpaRepository activityRepository,
            UserJpaRepository userRepository,
            ProposalService proposalService,
            PublicationService publicationService,
            WalletService walletService) {

        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.proposalService = proposalService;
        this.publicationService = publicationService;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityRequest request) {

        validateBasicData(request);

        Proposal proposal =
                proposalService.findProposalById(request.proposalId());

        if (!ProposalStatus.ACCEPTED.name().equals(proposal.getProposalStatus())) {
            throw new InvalidActivityStateException(
                    "only accepted proposals can be scheduled"
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

        UserEntity requester = findUser(requesterId);
        UserEntity participant = findUser(participantId);

        walletService.reserveBalance(requesterId, publication.getPheltAmount());

        ActivityEntity activity = new ActivityEntity();
        activity.setPublicationId(publication.getPublicationId());
        activity.setProposalId(proposal.getProposalId());
        activity.setRequester(requester);
        activity.setParticipant(participant);
        activity.setInteractionMode(publication.getInteractionMode());
        activity.setActivityDate(request.activityDate());
        activity.setActivityTime(request.activityTime());
        activity.setDurationMinutes(request.durationMinutes());
        activity.setLocation(request.location());
        activity.setVirtualLink(request.virtualLink());
        activity.setAgreedAmount(publication.getPheltAmount());
        activity.setCheckInCode(UUID.randomUUID().toString());
        activity.setActivityStatus(ActivityStatus.SCHEDULED.name());

        return activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityEntity findActivityById(Integer activityId) {

        return activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(activityId));
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

        ActivityEntity activity = findActivityById(activityId);

        ensureScheduled(activity, "completed");

        if (checkInCode == null || !checkInCode.equals(activity.getCheckInCode())) {
            throw new InvalidCheckInCodeException("check-in code does not match");
        }

        walletService.transferReservedBalance(
                activity.getRequester().getUserId(),
                activity.getParticipant().getUserId(),
                activity.getAgreedAmount()
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

        ActivityEntity activity = findActivityById(activityId);

        ensureScheduled(activity, "cancelled");

        walletService.releaseReservedBalance(
                activity.getRequester().getUserId(),
                activity.getAgreedAmount()
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
                    "only scheduled activities can be " + action
            );
        }
    }

    private void validateBasicData(ActivityRequest request) {

        if (request.proposalId() == null) {
            throw new InvalidActivityDataException("proposalId is required");
        }

        if (request.activityDate() == null) {
            throw new InvalidActivityDataException("activityDate is required");
        }

        if (request.activityDate().isBefore(LocalDate.now())) {
            throw new InvalidActivityDataException(
                    "activityDate must not be in the past"
            );
        }

        if (request.activityTime() == null) {
            throw new InvalidActivityDataException("activityTime is required");
        }

        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new InvalidActivityDataException(
                    "durationMinutes must be greater than zero"
            );
        }
    }

    private void validateModeData(ActivityRequest request, String interactionMode) {

        if (InteractionMode.PRESENTIAL.name().equals(interactionMode)) {
            requireText(request.location(), "location", MAX_LOCATION_LENGTH);
        }

        if (InteractionMode.VIRTUAL.name().equals(interactionMode)) {
            requireText(request.virtualLink(), "virtualLink", MAX_VIRTUAL_LINK_LENGTH);
        }
    }

    private void requireText(String value, String field, int maxLength) {

        if (value == null || value.isBlank()) {
            throw new InvalidActivityDataException(
                    field + " is required for this interaction mode"
            );
        }

        if (value.length() > maxLength) {
            throw new InvalidActivityDataException(
                    field + " must be at most " + maxLength + " characters"
            );
        }
    }
}
