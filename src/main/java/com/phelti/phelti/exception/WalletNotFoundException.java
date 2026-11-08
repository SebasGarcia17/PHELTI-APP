package com.phelti.phelti.exception;

public class WalletNotFoundException extends ResourceNotFoundException {

    public WalletNotFoundException(Integer userId) {
        super("Wallet not found for user with id: " + userId);
    }
}
