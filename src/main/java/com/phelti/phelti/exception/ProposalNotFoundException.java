package com.phelti.phelti.exception;

public class ProposalNotFoundException extends RuntimeException {

    public ProposalNotFoundException(Integer proposalId) {
        super("Proposal not found with id: " + proposalId);
    }
}
