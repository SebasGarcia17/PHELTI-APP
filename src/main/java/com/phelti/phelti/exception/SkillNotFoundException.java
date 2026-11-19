package com.phelti.phelti.exception;

public class SkillNotFoundException extends ResourceNotFoundException {

    public SkillNotFoundException(Integer skillId) {
        super("Skill not found with id: " + skillId);
    }
}
