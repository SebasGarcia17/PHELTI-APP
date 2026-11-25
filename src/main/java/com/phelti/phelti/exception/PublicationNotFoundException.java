package com.phelti.phelti.exception;

public class PublicationNotFoundException extends ResourceNotFoundException {

    public PublicationNotFoundException(Integer publicationId) {
        super("La publicación no existe.");
    }
}
