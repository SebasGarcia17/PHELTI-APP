package com.phelti.phelti.exception;

public class ProposalNotFoundException extends ResourceNotFoundException {

    public ProposalNotFoundException(Integer proposalId) {
        super("La propuesta no existe.");
    }
}
