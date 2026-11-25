package com.phelti.phelti.exception;

public class InterestNotAssignedException extends ResourceNotFoundException {

    public InterestNotAssignedException(Integer interestId) {
        super("Este interés no está asignado al usuario.");
    }
}
