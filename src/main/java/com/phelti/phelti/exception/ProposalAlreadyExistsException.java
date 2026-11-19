package com.phelti.phelti.exception;

public class ProposalAlreadyExistsException extends ConflictException {

    public ProposalAlreadyExistsException(Integer userId, Integer publicationId) {
        super("User " + userId + " already has an active proposal on publication " + publicationId);
    }
}
