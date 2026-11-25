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

    private final WalletService walletService;

    public UserServiceImpl(UserJpaRepository userRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public UserEntity createUser(String name, String email, String description) {

        name = name == null ? null : name.trim();
        email = email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
        validateUser(name, email);
        validateDescription(description);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        UserEntity user = new UserEntity();
        user.setName(name.trim());
        user.setEmail(email);
        user.setDescription(normalizeBlank(description));
        user.setAverageRating(BigDecimal.ZERO);

        UserEntity saved = userRepository.save(user);
        walletService.createWallet(saved.getUserId());
        return saved;
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
        validateDescription(description);

        user.setName(name.trim());
        user.setDescription(normalizeBlank(description));
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
                    "Selecciona una modalidad válida."
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
                    "Selecciona un día de la semana válido."
            );
        }
    }

    private String normalizeBlank(String value) {

        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateDescription(String value) {
        if (value != null && value.length() > 1500) throw new InvalidUserDataException("La descripción admite hasta 1500 caracteres.");
    }

    private void validateLocation(String location) {

        if (location != null && location.length() > MAX_LOCATION_LENGTH) {
            throw new InvalidUserDataException(
                    "La ubicación admite hasta " + MAX_LOCATION_LENGTH + " caracteres."
            );
        }
    }

    private void validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("El nombre es obligatorio.");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    "El nombre admite hasta " + MAX_NAME_LENGTH + " caracteres."
            );
        }
    }

    private void validateUser(String name, String email) {

        validateName(name);

        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("El correo electrónico es obligatorio.");
        }

        if (email.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(email).matches()) {

            throw new InvalidUserDataException(
                    "Introduce un correo válido de hasta " + MAX_EMAIL_LENGTH + " caracteres."
            );
        }
    }
}
