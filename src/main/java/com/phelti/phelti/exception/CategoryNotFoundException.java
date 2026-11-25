package com.phelti.phelti.exception;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException() {
        super("La categoría no existe.");
    }
}
