package com.phelti.phelti.exception;

public class InsufficientBalanceException extends ConflictException {

    public InsufficientBalanceException(Integer userId) {
        super("El solicitante no tiene suficientes phelts disponibles.");
    }
}
