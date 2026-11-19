package com.phelti.phelti.exception;

public class SkillAlreadyAssignedException extends ConflictException {

    public SkillAlreadyAssignedException(Integer skillId) {
        super("Skill is already assigned to the user, skill id: " + skillId);
    }
}
