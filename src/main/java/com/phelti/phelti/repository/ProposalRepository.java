package com.phelti.phelti.repository;

import com.phelti.phelti.model.Proposal;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository {

    List<Proposal> findAllProposals();

    Optional<Proposal> findProposalById(Integer proposalId);

    List<Proposal> findProposalsByPublicationId(Integer publicationId);

    Proposal createProposal(Proposal proposal);

    void updateProposalStatus(Integer proposalId, String status);

    void deleteProposal(Integer proposalId);
}
