package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
}
