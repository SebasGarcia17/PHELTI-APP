package com.phelti.phelti.exception;

public class ActivityAlreadyExistsException extends ConflictException {

    public ActivityAlreadyExistsException(Integer proposalId) {
        super("Esta propuesta ya tiene una actividad asociada.");
    }
}
