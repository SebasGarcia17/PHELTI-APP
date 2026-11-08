package com.phelti.phelti.exception;

public class ProposalNotFoundException extends ResourceNotFoundException {

    public ProposalNotFoundException(Integer proposalId) {
        super("Proposal not found with id: " + proposalId);
    }
}
