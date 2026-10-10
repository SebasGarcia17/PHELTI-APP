package com.phelti.phelti.service;

import com.phelti.phelti.model.Proposal;

import java.util.List;

public interface ProposalService {

    Proposal createProposal(Proposal proposal);

    Proposal findProposalById(Integer proposalId);

    List<Proposal> findAllProposals();

    Proposal acceptProposal(Integer proposalId);

    Proposal rejectProposal(Integer proposalId);

    void deleteProposal(Integer proposalId);
}
