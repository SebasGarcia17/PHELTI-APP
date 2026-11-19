package com.phelti.phelti.exception;

public class AvailabilityNotFoundException extends ResourceNotFoundException {

    public AvailabilityNotFoundException(Integer availabilityId) {
        super("Availability not found with id: " + availabilityId);
    }
}
