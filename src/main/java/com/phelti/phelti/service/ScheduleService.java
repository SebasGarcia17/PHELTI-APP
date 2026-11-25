package com.phelti.phelti.service;

import com.phelti.phelti.dto.ActivityRequest;
import com.phelti.phelti.dto.ScheduleOfferResponse;
import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.model.BusinessRules;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.repository.jpa.ActivityJpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {
    private final JdbcTemplate jdbc;
    private final ProposalService proposals;
    private final PublicationService publications;
    private final ActivityService activities;
    private final ActivityJpaRepository activityRepository;

    private final RowMapper<ScheduleOfferResponse> mapper = (rs, row) -> new ScheduleOfferResponse(
            rs.getInt("id_programacion"), rs.getInt("id_propuesta"), rs.getInt("id_solicitante"),
            rs.getObject("fecha", LocalDate.class), rs.getObject("hora", LocalTime.class), rs.getInt("duracion"),
            rs.getString("lugar"), rs.getString("enlace_virtual"), rs.getString("estado"),
            rs.getObject("fecha_creacion", LocalDateTime.class), rs.getObject("fecha_decision", LocalDateTime.class));

    public ScheduleService(JdbcTemplate jdbc, ProposalService proposals, PublicationService publications,
                           ActivityService activities, ActivityJpaRepository activityRepository) {
        this.jdbc = jdbc;
        this.proposals = proposals;
        this.publications = publications;
        this.activities = activities;
        this.activityRepository = activityRepository;
    }

    private record Agreement(Proposal proposal, Publication publication, Integer requester, Integer provider) { }

    private Agreement agreement(Integer proposalId, Integer actor, boolean lock) {
        Proposal proposal = proposals.findProposalById(proposalId);
        if (lock) {
            publications.lockPublication(proposal.getPublicationId());
            proposal = proposals.findProposalById(proposalId);
        }
        Publication publication = publications.findPublicationById(proposal.getPublicationId());
        boolean offers = "OFFERS_SUPPORT".equals(publication.getPublicationType());
        Integer requester = offers ? proposal.getProposingUserId() : publication.getUserId();
        Integer provider = offers ? publication.getUserId() : proposal.getProposingUserId();
        if (!actor.equals(requester) && !actor.equals(provider)) deny();
        return new Agreement(proposal, publication, requester, provider);
    }

    private void deny() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta acción corresponde a la otra persona del intercambio."); }

    private void available(Agreement agreement) {
        if (!"ACCEPTED".equals(agreement.proposal().getProposalStatus()) ||
                activityRepository.existsByProposalId(agreement.proposal().getProposalId())) {
            throw new InvalidActivityStateException("El acuerdo ya no admite una nueva programación.");
        }
    }

    private ScheduleOfferResponse find(Integer id) {
        return jdbc.query("SELECT * FROM programacion_propuesta WHERE id_programacion = ?", mapper, id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La solicitud de horario no existe."));
    }

    @Transactional(readOnly = true)
    public List<ScheduleOfferResponse> history(Integer proposalId, Integer actor) {
        agreement(proposalId, actor, false);
        return jdbc.query("SELECT * FROM programacion_propuesta WHERE id_propuesta = ? ORDER BY id_programacion DESC", mapper, proposalId);
    }

    private ActivityRequest normalize(ActivityRequest request, Publication publication) {
        boolean presencial = "PRESENTIAL".equals(publication.getInteractionMode());
        return new ActivityRequest(request.proposalId(), request.activityDate(),
                request.activityTime().withSecond(0).withNano(0), request.durationMinutes(),
                presencial && request.location() != null ? request.location().trim() : null,
                !presencial && request.virtualLink() != null ? request.virtualLink().trim() : null);
    }

    @Transactional
    public ScheduleOfferResponse propose(ActivityRequest request, Integer actor) {
        Agreement agreement = agreement(request.proposalId(), actor, true);
        available(agreement);
        if (!actor.equals(agreement.requester())) deny();
        activities.validateDraft(request);
        if (jdbc.queryForObject("SELECT count(*) FROM programacion_propuesta WHERE id_propuesta = ? AND estado = 'PENDING'", Long.class, request.proposalId()) > 0) {
            throw new InvalidActivityStateException("Ya existe una solicitud de horario pendiente. Retírala antes de proponer otro horario.");
        }
        ActivityRequest conditions = normalize(request, agreement.publication());
        Integer id = jdbc.queryForObject("""
                INSERT INTO programacion_propuesta (id_propuesta,id_solicitante,fecha,hora,duracion,lugar,enlace_virtual,estado,fecha_creacion)
                VALUES (?,?,?,?,?,?,?,'PENDING',?) RETURNING id_programacion
                """, Integer.class, request.proposalId(), actor, conditions.activityDate(), conditions.activityTime(), conditions.durationMinutes(),
                conditions.location(), conditions.virtualLink(), BusinessRules.now());
        return find(id);
    }

    @Transactional
    public ActivityEntity accept(Integer id, Integer actor) {
        ScheduleOfferResponse offer = find(id);
        Agreement agreement = agreement(offer.proposalId(), actor, true);
        return acceptLocked(find(id), agreement, actor);
    }

    @Transactional
    public ActivityEntity acceptMatching(ActivityRequest request, Integer actor) {
        Agreement agreement = agreement(request.proposalId(), actor, true);
        ScheduleOfferResponse offer = jdbc.query("SELECT * FROM programacion_propuesta WHERE id_propuesta = ? AND estado = 'PENDING'", mapper, request.proposalId())
                .stream().findFirst().orElseThrow(() -> new InvalidActivityStateException("Primero el solicitante debe proponer un horario."));
        if (!offer.conditions().equals(normalize(request, agreement.publication()))) throw new InvalidActivityStateException("Las condiciones no coinciden con la solicitud de horario vigente.");
        return acceptLocked(offer, agreement, actor);
    }

    private ActivityEntity acceptLocked(ScheduleOfferResponse offer, Agreement agreement, Integer actor) {
        available(agreement);
        if (!actor.equals(agreement.provider())) deny();
        if (!"PENDING".equals(offer.status())) throw new InvalidActivityStateException("Esta solicitud de horario ya fue resuelta.");
        ActivityEntity activity = activities.createActivity(offer.conditions());
        jdbc.update("UPDATE programacion_propuesta SET estado = 'ACCEPTED', fecha_decision = ? WHERE id_programacion = ?", BusinessRules.now(), offer.scheduleId());
        return activity;
    }

    @Transactional
    public void cancelAgreement(Integer proposalId, Integer actor) {
        Agreement agreement = agreement(proposalId, actor, true);
        available(agreement);
        jdbc.update("UPDATE programacion_propuesta SET estado = 'WITHDRAWN', fecha_decision = ? WHERE id_propuesta = ? AND estado = 'PENDING'", BusinessRules.now(), proposalId);
        proposals.cancelAcceptedProposal(proposalId);
        publications.changePublicationStatus(agreement.publication().getPublicationId(), "PUBLISHED");
    }

    @Transactional
    public ScheduleOfferResponse decline(Integer id, Integer actor) {
        ScheduleOfferResponse offer = find(id);
        Agreement agreement = agreement(offer.proposalId(), actor, true);
        offer = find(id);
        available(agreement);
        if (!"PENDING".equals(offer.status())) throw new InvalidActivityStateException("Esta solicitud de horario ya fue resuelta.");
        jdbc.update("UPDATE programacion_propuesta SET estado = ?, fecha_decision = ? WHERE id_programacion = ?",
                actor.equals(agreement.requester()) ? "WITHDRAWN" : "REJECTED", BusinessRules.now(), id);
        return find(id);
    }
}
