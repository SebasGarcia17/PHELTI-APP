package com.phelti.phelti.config;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.service.ActivityService;
import com.phelti.phelti.service.ProposalService;
import com.phelti.phelti.service.PublicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class DemoAccess {
    private final String key;
    private final ProposalService proposals;
    private final PublicationService publications;
    private final ActivityService activities;
    private final JdbcTemplate jdbc;

    public DemoAccess(@Value("${phelti.demo.key:}") String key, ProposalService proposals,
                      PublicationService publications, ActivityService activities, JdbcTemplate jdbc) {
        this.key = key;
        this.proposals = proposals;
        this.publications = publications;
        this.activities = activities;
        this.jdbc = jdbc;
    }

    private HttpServletRequest request() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private boolean trusted() {
        String supplied = request().getHeader("X-Phelti-Key");
        return key.length() >= 32 && supplied != null && MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    public void requireGateway() {
        if (!trusted()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo verificar el acceso.");
    }

    public Integer optionalActor() {
        if (!trusted()) return null;
        try {
            int id = Integer.parseInt(request().getHeader("X-Phelti-Actor"));
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) { return null; }
    }

    public Integer actor() {
        requireGateway();
        Integer id = optionalActor();
        if (id == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Selecciona una cuenta para continuar.");
        return id;
    }

    public void own(Integer owner) {
        if (!actor().equals(owner)) denied();
    }

    public void publicationOwner(Integer id) { own(publications.findPublicationById(id).getUserId()); }
    public void proposalOwner(Integer id) { publicationOwner(proposals.findProposalById(id).getPublicationId()); }
    public void proposer(Integer id) { own(proposals.findProposalById(id).getProposingUserId()); }

    public boolean belongsToProposal(Integer user, Proposal proposal) {
        return user.equals(proposal.getProposingUserId()) || user.equals(publications.findPublicationById(proposal.getPublicationId()).getUserId());
    }

    public void proposalParticipant(Integer id) {
        if (!belongsToProposal(actor(), proposals.findProposalById(id))) denied();
    }

    public void activityParticipant(Integer id) {
        Integer user = actor();
        ActivityEntity activity = activities.findActivityById(id);
        if (!user.equals(activity.getRequester().getUserId()) && !user.equals(activity.getParticipant().getUserId())) denied();
    }

    public void availabilityOwner(Integer id) {
        Integer user = actor();
        var owners = jdbc.query("SELECT id_usuario FROM disponibilidad WHERE id_disponibilidad = ?", (rs, n) -> rs.getInt(1), id);
        if (owners.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El horario no existe.");
        if (!user.equals(owners.get(0))) denied();
    }

    private void denied() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta operación."); }
}
