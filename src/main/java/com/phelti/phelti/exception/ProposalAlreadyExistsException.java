package com.phelti.phelti.exception;

public class ProposalAlreadyExistsException extends ConflictException {

    public ProposalAlreadyExistsException(Integer userId, Integer publicationId) {
        super("Ya existe una propuesta activa para esta publicación.");
    }
}
