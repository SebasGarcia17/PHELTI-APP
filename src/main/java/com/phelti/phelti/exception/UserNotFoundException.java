package com.phelti.phelti.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Integer userId) {
        super("El usuario no existe.");
    }
}
