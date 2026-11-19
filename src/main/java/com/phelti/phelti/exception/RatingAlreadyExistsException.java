package com.phelti.phelti.exception;

public class RatingAlreadyExistsException extends ConflictException {

    public RatingAlreadyExistsException(Integer raterId) {
        super("The user already rated this activity, user id: " + raterId);
    }
}
