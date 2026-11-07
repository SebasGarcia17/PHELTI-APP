package com.phelti.phelti.exception;

public class WalletAlreadyExistsException extends RuntimeException {

    public WalletAlreadyExistsException(Integer userId) {
        super("Wallet already exists for user with id: " + userId);
    }
}