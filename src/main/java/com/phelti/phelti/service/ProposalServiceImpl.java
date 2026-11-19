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
import java.util.List;

@Service
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final PublicationService publicationService;

    public ProposalServiceImpl(
            ProposalRepository proposalRepository,
            PublicationService publicationService) {

        this.proposalRepository = proposalRepository;
        this.publicationService = publicationService;
    }

    @Override
    public Proposal createProposal(Proposal proposal) {

        validateProposal(proposal);

        Publication publication =
                publicationService.findPublicationById(
                        proposal.getPublicationId()
                );

        if (!PublicationStatus.PUBLISHED.name()
                .equals(publication.getPublicationStatus())) {

            throw new InvalidProposalDataException(
                    "publication is not available for proposals"
            );
        }

        if (proposal.getProposingUserId().equals(publication.getUserId())) {
            throw new InvalidProposalDataException(
                    "a user cannot propose on their own publication"
            );
        }

        ensureNoActiveProposalFromUser(proposal);
        validateProposedAmount(proposal, publication);

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

        if (!ProposalStatus.PENDING.name()
                .equals(proposal.getProposalStatus())) {

            throw new InvalidProposalDataException(
                    "only pending proposals can be accepted"
            );
        }

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

        if (!ProposalStatus.PENDING.name()
                .equals(proposal.getProposalStatus())) {

            throw new InvalidProposalDataException(
                    "only pending proposals can be rejected"
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
                    "only accepted proposals can be cancelled"
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

        findProposalById(proposalId);

        proposalRepository.deleteProposal(proposalId);
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

        if (proposedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProposalDataException(
                    "proposedAmount must be greater than zero"
            );
        }

        if (!publication.isNegotiable()) {
            throw new InvalidProposalDataException(
                    "the publication is not negotiable, proposedAmount is not allowed"
            );
        }
    }

    private void validateProposal(Proposal proposal) {

        if (proposal.getPublicationId() == null) {
            throw new InvalidProposalDataException(
                    "publicationId is required"
            );
        }

        if (proposal.getProposingUserId() == null) {
            throw new InvalidProposalDataException(
                    "proposingUserId is required"
            );
        }

        if (proposal.getMessage() != null
                && proposal.getMessage().length() > 500) {

            throw new InvalidProposalDataException(
                    "message must be at most 500 characters"
            );
        }
    }
}
