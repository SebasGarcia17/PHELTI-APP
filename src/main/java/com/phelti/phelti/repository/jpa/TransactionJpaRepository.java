package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Integer> {

    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.wallet.user.userId = :userId
            ORDER BY t.createdAt DESC, t.transactionId DESC
            """)
    List<TransactionEntity> findByUserId(@Param("userId") Integer userId);
}
