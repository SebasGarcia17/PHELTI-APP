package com.phelti.phelti.exception;

public class InsufficientBalanceException extends ConflictException {

    public InsufficientBalanceException(Integer userId) {
        super("Insufficient available balance for user with id: " + userId);
    }
}
