package com.phelti.phelti.exception;

public class SkillAlreadyExistsException extends ConflictException {

    public SkillAlreadyExistsException(String name) {
        super("Skill already exists: " + name);
    }
}
