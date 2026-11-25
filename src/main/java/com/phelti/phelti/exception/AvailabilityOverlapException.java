package com.phelti.phelti.exception;

public class AvailabilityOverlapException extends ConflictException {

    public AvailabilityOverlapException(String message) {
        super("Este horario se cruza con otro horario registrado.");
    }
}
