package com.phelti.phelti.service;

import com.phelti.phelti.entity.SkillEntity;

import java.util.List;

public interface SkillService {

    SkillEntity createSkill(String name);

    List<SkillEntity> findAllSkills();

    List<SkillEntity> findSkillsByUserId(Integer userId);

    void addSkillToUser(Integer userId, Integer skillId);

    void removeSkillFromUser(Integer userId, Integer skillId);
}
