package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;

import java.time.LocalTime;
import java.util.List;

public interface AvailabilityService {

    AvailabilityEntity addAvailability(
            Integer userId,
            String dayOfWeek,
            LocalTime startTime,
            LocalTime endTime);

    List<AvailabilityEntity> findAvailabilityByUserId(Integer userId);

    void deleteAvailability(Integer availabilityId);
}
