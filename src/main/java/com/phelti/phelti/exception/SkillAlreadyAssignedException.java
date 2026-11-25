package com.phelti.phelti.exception;

public class SkillAlreadyAssignedException extends ConflictException {

    public SkillAlreadyAssignedException(Integer skillId) {
        super("Ya tienes esta habilidad registrada.");
    }
}
