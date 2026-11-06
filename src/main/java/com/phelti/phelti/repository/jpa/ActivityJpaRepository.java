package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, Integer> {
}
