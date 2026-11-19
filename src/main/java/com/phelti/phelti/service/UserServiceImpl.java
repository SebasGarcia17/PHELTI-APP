package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.EmailAlreadyRegisteredException;
import com.phelti.phelti.exception.InvalidUserDataException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_LOCATION_LENGTH = 150;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserJpaRepository userRepository;

    public UserServiceImpl(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserEntity createUser(String name, String email, String description) {

        validateUser(name, email);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setDescription(description);
        user.setAverageRating(BigDecimal.ZERO);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findUserById(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> findAllUsers() {

        return userRepository.findAll();
    }

    @Override
    @Transactional
    public UserEntity updateUserProfile(
            Integer userId,
            String name,
            String description,
            String location,
            String preferredInteractionMode) {

        UserEntity user = findUserById(userId);

        validateName(name);
        validateLocation(location);

        user.setName(name);
        user.setDescription(description);
        user.setLocation(normalizeBlank(location));
        user.setPreferredInteractionMode(
                parseInteractionMode(preferredInteractionMode)
        );

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> searchUsers(
            Integer skillId,
            Integer interestId,
            String interactionMode,
            String dayOfWeek) {

        String mode = parseInteractionMode(interactionMode);
        String day = parseDayOfWeek(dayOfWeek);

        return userRepository.findAll().stream()
                .filter(user -> skillId == null || user.getSkills().stream()
                        .anyMatch(skill -> skillId.equals(skill.getSkillId())))
                .filter(user -> interestId == null || user.getInterests().stream()
                        .anyMatch(interest -> interestId.equals(interest.getInterestId())))
                .filter(user -> mode == null || mode.equals(user.getPreferredInteractionMode()))
                .filter(user -> day == null || user.getAvailabilities().stream()
                        .anyMatch(slot -> day.equals(slot.getDayOfWeek())))
                .toList();
    }

    private String parseInteractionMode(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();

        try {
            return InteractionMode.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserDataException(
                    "interactionMode must be VIRTUAL or PRESENTIAL"
            );
        }
    }

    private String parseDayOfWeek(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserDataException(
                    "dayOfWeek must be a day such as MONDAY or FRIDAY"
            );
        }
    }

    private String normalizeBlank(String value) {

        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateLocation(String location) {

        if (location != null && location.length() > MAX_LOCATION_LENGTH) {
            throw new InvalidUserDataException(
                    "location must be at most " + MAX_LOCATION_LENGTH + " characters"
            );
        }
    }

    private void validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("name must not be empty");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    "name must be at most " + MAX_NAME_LENGTH + " characters"
            );
        }
    }

    private void validateUser(String name, String email) {

        validateName(name);

        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("email must not be empty");
        }

        if (email.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(email).matches()) {

            throw new InvalidUserDataException(
                    "email must be a valid address of at most "
                            + MAX_EMAIL_LENGTH + " characters"
            );
        }
    }
}
