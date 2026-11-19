package com.phelti.phelti.exception;

public class InterestNotAssignedException extends ResourceNotFoundException {

    public InterestNotAssignedException(Integer interestId) {
        super("Interest is not assigned to the user, interest id: " + interestId);
    }
}
