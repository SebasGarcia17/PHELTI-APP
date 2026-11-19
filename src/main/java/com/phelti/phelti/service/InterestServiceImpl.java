package com.phelti.phelti.service;

import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.InvalidInterestDataException;
import com.phelti.phelti.exception.InterestAlreadyAssignedException;
import com.phelti.phelti.exception.InterestAlreadyExistsException;
import com.phelti.phelti.exception.InterestNotAssignedException;
import com.phelti.phelti.exception.InterestNotFoundException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.InterestJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterestServiceImpl implements InterestService {

    private static final int MAX_NAME_LENGTH = 80;

    private final InterestJpaRepository interestRepository;
    private final UserJpaRepository userRepository;

    public InterestServiceImpl(
            InterestJpaRepository interestRepository,
            UserJpaRepository userRepository) {

        this.interestRepository = interestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public InterestEntity createInterest(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidInterestDataException("name must not be empty");
        }

        String cleanName = name.trim();

        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new InvalidInterestDataException(
                    "name must be at most " + MAX_NAME_LENGTH + " characters"
            );
        }

        if (interestRepository.existsByNameIgnoreCase(cleanName)) {
            throw new InterestAlreadyExistsException(cleanName);
        }

        InterestEntity interest = new InterestEntity();
        interest.setName(cleanName);

        return interestRepository.save(interest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestEntity> findAllInterests() {

        return interestRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestEntity> findInterestsByUserId(Integer userId) {

        UserEntity user = findUser(userId);

        return new ArrayList<>(user.getInterests());
    }

    @Override
    @Transactional
    public void addInterestToUser(Integer userId, Integer interestId) {

        UserEntity user = findUser(userId);
        InterestEntity interest = findInterest(interestId);

        if (user.getInterests().contains(interest)) {
            throw new InterestAlreadyAssignedException(interestId);
        }

        user.getInterests().add(interest);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeInterestFromUser(Integer userId, Integer interestId) {

        UserEntity user = findUser(userId);
        InterestEntity interest = findInterest(interestId);

        if (!user.getInterests().remove(interest)) {
            throw new InterestNotAssignedException(interestId);
        }

        userRepository.save(user);
    }

    private UserEntity findUser(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private InterestEntity findInterest(Integer interestId) {

        return interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestNotFoundException(interestId));
    }
}
