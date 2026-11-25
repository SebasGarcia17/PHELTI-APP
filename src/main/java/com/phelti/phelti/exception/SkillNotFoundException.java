package com.phelti.phelti.exception;

public class SkillNotFoundException extends ResourceNotFoundException {

    public SkillNotFoundException(Integer skillId) {
        super("La habilidad no existe.");
    }
}
