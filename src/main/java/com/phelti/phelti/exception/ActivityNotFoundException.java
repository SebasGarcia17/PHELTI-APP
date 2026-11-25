package com.phelti.phelti.exception;

public class ActivityNotFoundException extends ResourceNotFoundException {

    public ActivityNotFoundException(Integer activityId) {
        super("La actividad no existe.");
    }
}
