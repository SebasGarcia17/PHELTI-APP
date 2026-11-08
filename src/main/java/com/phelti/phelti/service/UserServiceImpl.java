package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.EmailAlreadyRegisteredException;
import com.phelti.phelti.exception.InvalidUserDataException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
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

    private void validateUser(String name, String email) {

        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException("name must not be empty");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    "name must be at most " + MAX_NAME_LENGTH + " characters"
            );
        }

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
