package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Integer> {

    Optional<WalletEntity> findByUserUserId(Integer userId);

    boolean existsByUserUserId(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.user.userId = :userId")
    Optional<WalletEntity> findForUpdateByUserId(@Param("userId") Integer userId);
}
