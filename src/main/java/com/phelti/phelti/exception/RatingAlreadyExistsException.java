package com.phelti.phelti.exception;

public class RatingAlreadyExistsException extends ConflictException {

    public RatingAlreadyExistsException(Integer raterId) {
        super("Ya calificaste esta actividad.");
    }
}
