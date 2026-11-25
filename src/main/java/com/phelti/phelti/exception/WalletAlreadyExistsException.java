package com.phelti.phelti.exception;

public class WalletAlreadyExistsException extends ConflictException {

    public WalletAlreadyExistsException(Integer userId) {
        super("El usuario ya tiene una billetera.");
    }
}
