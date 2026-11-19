package com.phelti.phelti.exception;

public class ConfirmationAlreadyExistsException extends ConflictException {

    public ConfirmationAlreadyExistsException(Integer userId) {
        super("The user already confirmed this activity, user id: " + userId);
    }
}
