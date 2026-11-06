package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Integer> {
}
