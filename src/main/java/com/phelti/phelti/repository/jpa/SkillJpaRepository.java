package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillJpaRepository extends JpaRepository<SkillEntity, Integer> {

    List<SkillEntity> findAllByOrderByNameAsc();
}
