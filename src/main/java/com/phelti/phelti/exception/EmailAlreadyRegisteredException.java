package com.phelti.phelti.exception;

public class EmailAlreadyRegisteredException extends ConflictException {

    public EmailAlreadyRegisteredException(String email) {
        super("Este correo electrónico ya está registrado.");
    }
}
