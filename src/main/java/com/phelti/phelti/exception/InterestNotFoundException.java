package com.phelti.phelti.exception;

public class InterestNotFoundException extends ResourceNotFoundException {

    public InterestNotFoundException(Integer interestId) {
        super("El interés no existe.");
    }
}
