package com.phelti.phelti.exception;

public abstract class InvalidRequestException extends RuntimeException {

    protected InvalidRequestException(String message) {
        super(message);
    }
}
