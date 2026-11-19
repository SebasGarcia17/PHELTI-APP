package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.AvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityJpaRepository extends JpaRepository<AvailabilityEntity, Integer> {

    List<AvailabilityEntity> findByUserUserId(Integer userId);

    List<AvailabilityEntity> findByUserUserIdAndDayOfWeek(Integer userId, String dayOfWeek);
}
