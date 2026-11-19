package com.phelti.phelti.exception;

public class InterestAlreadyAssignedException extends ConflictException {

    public InterestAlreadyAssignedException(Integer interestId) {
        super("Interest is already assigned to the user, interest id: " + interestId);
    }
}
