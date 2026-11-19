package com.phelti.phelti.exception;

public class SkillNotAssignedException extends ResourceNotFoundException {

    public SkillNotAssignedException(Integer skillId) {
        super("Skill is not assigned to the user, skill id: " + skillId);
    }
}
