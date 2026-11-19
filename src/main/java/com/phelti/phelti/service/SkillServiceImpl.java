package com.phelti.phelti.service;

import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.InvalidSkillDataException;
import com.phelti.phelti.exception.SkillAlreadyAssignedException;
import com.phelti.phelti.exception.SkillAlreadyExistsException;
import com.phelti.phelti.exception.SkillNotAssignedException;
import com.phelti.phelti.exception.SkillNotFoundException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.SkillJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {

    private static final int MAX_NAME_LENGTH = 80;

    private final SkillJpaRepository skillRepository;
    private final UserJpaRepository userRepository;

    public SkillServiceImpl(
            SkillJpaRepository skillRepository,
            UserJpaRepository userRepository) {

        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SkillEntity createSkill(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidSkillDataException("name must not be empty");
        }

        String cleanName = name.trim();

        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new InvalidSkillDataException(
                    "name must be at most " + MAX_NAME_LENGTH + " characters"
            );
        }

        if (skillRepository.existsByNameIgnoreCase(cleanName)) {
            throw new SkillAlreadyExistsException(cleanName);
        }

        SkillEntity skill = new SkillEntity();
        skill.setName(cleanName);

        return skillRepository.save(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillEntity> findAllSkills() {

        return skillRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillEntity> findSkillsByUserId(Integer userId) {

        UserEntity user = findUser(userId);

        return new ArrayList<>(user.getSkills());
    }

    @Override
    @Transactional
    public void addSkillToUser(Integer userId, Integer skillId) {

        UserEntity user = findUser(userId);
        SkillEntity skill = findSkill(skillId);

        if (user.getSkills().contains(skill)) {
            throw new SkillAlreadyAssignedException(skillId);
        }

        user.getSkills().add(skill);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeSkillFromUser(Integer userId, Integer skillId) {

        UserEntity user = findUser(userId);
        SkillEntity skill = findSkill(skillId);

        if (!user.getSkills().remove(skill)) {
            throw new SkillNotAssignedException(skillId);
        }

        userRepository.save(user);
    }

    private UserEntity findUser(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private SkillEntity findSkill(Integer skillId) {

        return skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException(skillId));
    }
}
