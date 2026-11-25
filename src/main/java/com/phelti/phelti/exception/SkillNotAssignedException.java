package com.phelti.phelti.exception;

public class SkillNotAssignedException extends ResourceNotFoundException {

    public SkillNotAssignedException(Integer skillId) {
        super("Esta habilidad no está asignada al usuario.");
    }
}
