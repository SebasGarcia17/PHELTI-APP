package com.phelti.phelti.exception;

public class InterestAlreadyExistsException extends ConflictException {

    public InterestAlreadyExistsException(String name) {
        super("Interest already exists: " + name);
    }
}
