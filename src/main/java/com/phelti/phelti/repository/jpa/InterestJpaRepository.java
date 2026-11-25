package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.InterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestJpaRepository extends JpaRepository<InterestEntity, Integer> {

    List<InterestEntity> findAllByOrderByNameAsc();
}
