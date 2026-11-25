package com.phelti.phelti.exception;

public class InterestAlreadyAssignedException extends ConflictException {

    public InterestAlreadyAssignedException(Integer interestId) {
        super("Ya tienes este interés registrado.");
    }
}
