package com.phelti.phelti.exception;

public class ActivityAlreadyExistsException extends ConflictException {

    public ActivityAlreadyExistsException(Integer proposalId) {
        super("An activity already exists for proposal with id: " + proposalId);
    }
}
