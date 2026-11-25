package com.phelti.phelti.exception;

public class ConfirmationAlreadyExistsException extends ConflictException {

    public ConfirmationAlreadyExistsException(Integer userId) {
        super("Ya confirmaste esta actividad.");
    }
}
