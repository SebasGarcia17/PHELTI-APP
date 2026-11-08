package com.phelti.phelti.exception;

public class ActivityNotFoundException extends ResourceNotFoundException {

    public ActivityNotFoundException(Integer activityId) {
        super("Activity not found with id: " + activityId);
    }
}
