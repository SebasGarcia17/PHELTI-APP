package com.phelti.phelti.exception;

public class PublicationNotFoundException extends ResourceNotFoundException {

    public PublicationNotFoundException(Integer publicationId) {
        super("Publication not found with id: " + publicationId);
    }
}
