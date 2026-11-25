package com.phelti.phelti.exception;

public class WalletNotFoundException extends ResourceNotFoundException {

    public WalletNotFoundException(Integer userId) {
        super("El participante todavía no tiene una billetera.");
    }
}
