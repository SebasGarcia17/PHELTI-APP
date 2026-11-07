package com.phelti.phelti.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Integer userId) {
        super("Wallet not found for user with id: " + userId);
    }
}