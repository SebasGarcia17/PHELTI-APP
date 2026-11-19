package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.AvailabilityNotFoundException;
import com.phelti.phelti.exception.AvailabilityOverlapException;
import com.phelti.phelti.exception.InvalidAvailabilityDataException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.AvailabilityJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityJpaRepository availabilityRepository;
    private final UserJpaRepository userRepository;

    public AvailabilityServiceImpl(
            AvailabilityJpaRepository availabilityRepository,
            UserJpaRepository userRepository) {

        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AvailabilityEntity addAvailability(
            Integer userId,
            String dayOfWeek,
            LocalTime startTime,
            LocalTime endTime) {

        String day = parseDayOfWeek(dayOfWeek);
        validateTimes(startTime, endTime);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        ensureNoOverlap(userId, day, startTime, endTime);

        AvailabilityEntity availability = new AvailabilityEntity();
        availability.setUser(user);
        availability.setDayOfWeek(day);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);

        return availabilityRepository.save(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityEntity> findAvailabilityByUserId(Integer userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return availabilityRepository.findByUserUserId(userId).stream()
                .sorted(Comparator
                        .comparing((AvailabilityEntity slot) -> DayOfWeek.valueOf(slot.getDayOfWeek()))
                        .thenComparing(AvailabilityEntity::getStartTime))
                .toList();
    }

    @Override
    @Transactional
    public void deleteAvailability(Integer availabilityId) {

        AvailabilityEntity availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

        availabilityRepository.delete(availability);
    }

    private String parseDayOfWeek(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidAvailabilityDataException("dayOfWeek is required");
        }

        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new InvalidAvailabilityDataException(
                    "dayOfWeek must be a day such as MONDAY or FRIDAY"
            );
        }
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime) {

        if (startTime == null || endTime == null) {
            throw new InvalidAvailabilityDataException(
                    "startTime and endTime are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidAvailabilityDataException(
                    "endTime must be after startTime"
            );
        }
    }

    private void ensureNoOverlap(
            Integer userId,
            String day,
            LocalTime startTime,
            LocalTime endTime) {

        boolean overlaps = availabilityRepository
                .findByUserUserIdAndDayOfWeek(userId, day)
                .stream()
                .anyMatch(slot -> startTime.isBefore(slot.getEndTime())
                        && endTime.isAfter(slot.getStartTime()));

        if (overlaps) {
            throw new AvailabilityOverlapException(
                    "the time slot overlaps an existing availability on " + day
            );
        }
    }
}
