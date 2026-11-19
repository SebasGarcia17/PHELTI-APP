package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.ConfirmationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfirmationJpaRepository extends JpaRepository<ConfirmationEntity, Integer> {

    boolean existsByActivityActivityIdAndUserUserId(Integer activityId, Integer userId);

    long countByActivityActivityId(Integer activityId);

    @Query("""
            SELECT c FROM ConfirmationEntity c
            JOIN FETCH c.activity
            JOIN FETCH c.user
            WHERE c.activity.activityId = :activityId
            ORDER BY c.confirmationId
            """)
    List<ConfirmationEntity> findByActivityId(@Param("activityId") Integer activityId);
}
