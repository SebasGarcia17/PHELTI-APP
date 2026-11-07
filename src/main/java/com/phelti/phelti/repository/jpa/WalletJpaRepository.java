package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Integer> {

    Optional<WalletEntity> findByUserUserId(Integer userId);

    boolean existsByUserUserId(Integer userId);
}
