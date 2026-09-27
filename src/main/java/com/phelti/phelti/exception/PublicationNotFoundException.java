package com.phelti.phelti.exception;

public class PublicationNotFoundException extends RuntimeException {

    public PublicationNotFoundException(Integer publicationId) {
        super("Publication not found with id: " + publicationId);
    }
}
