package com.phelti.phelti.service;

import com.phelti.phelti.exception.InvalidProposalDataException;
import com.phelti.phelti.exception.ProposalAlreadyExistsException;
import com.phelti.phelti.exception.ProposalNotFoundException;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.model.ProposalStatus;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.repository.ProposalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.phelti.phelti.model.BusinessRules;
import java.util.List;

@Service
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final PublicationService publicationService;
    private final WalletService walletService;

    public ProposalServiceImpl(
            ProposalRepository proposalRepository,
            PublicationService publicationService, WalletService walletService) {
        this.walletService = walletService;

        this.proposalRepository = proposalRepository;
        this.publicationService = publicationService;
    }

    @Override
    @Transactional
    public Proposal createProposal(Proposal proposal) {

        validateProposal(proposal);
        publicationService.lockPublication(proposal.getPublicationId());

        Publication publication =
                publicationService.findPublicationById(
                        proposal.getPublicationId()
                );

        if (!PublicationStatus.PUBLISHED.name()
                .equals(publication.getPublicationStatus())) {

            throw new InvalidProposalDataException(
                    "La publicación ya no recibe propuestas."
            );
        }

        if (proposal.getProposingUserId().equals(publication.getUserId())) {
            throw new InvalidProposalDataException(
                    "No puedes enviar propuestas a tu propia publicación."
            );
        }

        ensureNoActiveProposalFromUser(proposal);
        validateProposedAmount(proposal, publication);
        if (proposal.getProposedAmount() == null) proposal.setProposedAmount(publication.getPheltAmount());
        validateBalance(proposal, publication);

        proposal.setProposalStatus(
                ProposalStatus.PENDING.name()
        );

        return proposalRepository.createProposal(proposal);
    }

    @Override
    public Proposal findProposalById(Integer proposalId) {

        return proposalRepository.findProposalById(proposalId)
                .orElseThrow(
                        () -> new ProposalNotFoundException(proposalId)
                );
    }

    @Override
    public List<Proposal> findAllProposals() {

        return proposalRepository.findAllProposals();
    }

    @Override
    @Transactional
    public Proposal acceptProposal(Integer proposalId) {

        Proposal proposal = findProposalById(proposalId);
        publicationService.lockPublication(proposal.getPublicationId());
        proposal = findProposalById(proposalId);

        if (!ProposalStatus.PENDING.name()
                .equals(proposal.getProposalStatus())) {

            throw new InvalidProposalDataException(
                    "Solo se pueden aceptar propuestas pendientes."
            );
        }

        Publication publication = publicationService.findPublicationById(proposal.getPublicationId());
        if (!PublicationStatus.PUBLISHED.name().equals(publication.getPublicationStatus())) {
            throw new InvalidProposalDataException("La publicación ya no está disponible.");
        }
        validateBalance(proposal, publication);
        proposalRepository.updateProposalStatus(
                proposalId,
                ProposalStatus.ACCEPTED.name()
        );

        publicationService.changePublicationStatus(
                proposal.getPublicationId(),
                PublicationStatus.IN_PROGRESS.name()
        );

        rejectOtherPendingProposals(proposal);

        proposal.setProposalStatus(
                ProposalStatus.ACCEPTED.name()
        );

        return proposal;
    }

    @Override
    @Transactional
    public Proposal rejectProposal(Integer proposalId) {

        Proposal proposal = findProposalById(proposalId);
        publicationService.lockPublication(proposal.getPublicationId());
        proposal = findProposalById(proposalId);

        if (!ProposalStatus.PENDING.name()
                .equals(proposal.getProposalStatus())) {

            throw new InvalidProposalDataException(
                    "Solo se pueden rechazar propuestas pendientes."
            );
        }

        proposalRepository.updateProposalStatus(
                proposalId,
                ProposalStatus.REJECTED.name()
        );

        proposal.setProposalStatus(
                ProposalStatus.REJECTED.name()
        );

        return proposal;
    }

    @Override
    @Transactional
    public Proposal cancelAcceptedProposal(Integer proposalId) {

        Proposal proposal = findProposalById(proposalId);

        if (!ProposalStatus.ACCEPTED.name()
                .equals(proposal.getProposalStatus())) {

            throw new InvalidProposalDataException(
                    "Solo se pueden cancelar propuestas aceptadas."
            );
        }

        proposalRepository.updateProposalStatus(
                proposalId,
                ProposalStatus.CANCELLED.name()
        );

        proposal.setProposalStatus(
                ProposalStatus.CANCELLED.name()
        );

        return proposal;
    }

    @Override
    @Transactional
    public void deleteProposal(Integer proposalId) {

        Proposal proposal = findProposalById(proposalId);
        publicationService.lockPublication(proposal.getPublicationId());
        proposal = findProposalById(proposalId);
        if (!ProposalStatus.PENDING.name().equals(proposal.getProposalStatus())) {
            throw new InvalidProposalDataException("Solo se pueden retirar propuestas pendientes.");
        }
        proposalRepository.updateProposalStatus(proposalId, ProposalStatus.WITHDRAWN.name());
    }

    private void ensureNoActiveProposalFromUser(Proposal proposal) {
        boolean alreadyProposed = proposalRepository
                .findProposalsByPublicationId(proposal.getPublicationId())
                .stream()
                .anyMatch(existing ->
                        proposal.getProposingUserId().equals(existing.getProposingUserId())
                                && (ProposalStatus.PENDING.name().equals(existing.getProposalStatus())
                                || ProposalStatus.ACCEPTED.name().equals(existing.getProposalStatus())));

        if (alreadyProposed) {
            throw new ProposalAlreadyExistsException(
                    proposal.getProposingUserId(),
                    proposal.getPublicationId()
            );
        }
    }

    private void rejectOtherPendingProposals(
            Proposal acceptedProposal) {

        List<Proposal> proposals =
                proposalRepository.findProposalsByPublicationId(
                        acceptedProposal.getPublicationId()
                );

        for (Proposal candidate : proposals) {

            boolean isSameProposal =
                    candidate.getProposalId()
                            .equals(acceptedProposal.getProposalId());

            boolean isPending =
                    ProposalStatus.PENDING.name()
                            .equals(candidate.getProposalStatus());

            if (!isSameProposal && isPending) {

                proposalRepository.updateProposalStatus(
                        candidate.getProposalId(),
                        ProposalStatus.REJECTED.name()
                );
            }
        }
    }

    private void validateProposedAmount(
            Proposal proposal,
            Publication publication) {

        BigDecimal proposedAmount = proposal.getProposedAmount();

        if (proposedAmount == null) {
            return;
        }

        if (!BusinessRules.validAmount(proposedAmount)) {
            throw new InvalidProposalDataException(
                    "El importe propuesto debe ser positivo y tener hasta dos decimales."
            );
        }

        if (!publication.isNegotiable()) {
            throw new InvalidProposalDataException(
                    "Esta publicación tiene un precio fijo."
            );
        }
    }

    private void validateBalance(Proposal proposal, Publication publication) {
        Integer payer = com.phelti.phelti.model.PublicationType.OFFERS_SUPPORT.name().equals(publication.getPublicationType())
                ? proposal.getProposingUserId() : publication.getUserId();
        BigDecimal amount = proposal.getProposedAmount() != null ? proposal.getProposedAmount() : publication.getPheltAmount();
        if (walletService.findWalletByUserId(payer).getAvailableBalance().compareTo(amount) < 0) {
            throw new com.phelti.phelti.exception.InsufficientBalanceException(payer);
        }
    }

    private void validateProposal(Proposal proposal) {

        if (proposal.getPublicationId() == null) {
            throw new InvalidProposalDataException(
                    "Selecciona una publicación."
            );
        }

        if (proposal.getProposingUserId() == null) {
            throw new InvalidProposalDataException(
                    "Identifica a la persona que envía la propuesta."
            );
        }

        if (proposal.getMessage() != null
                && proposal.getMessage().length() > 500) {

            throw new InvalidProposalDataException(
                    "El mensaje admite hasta 500 caracteres."
            );
        }
    }
}
