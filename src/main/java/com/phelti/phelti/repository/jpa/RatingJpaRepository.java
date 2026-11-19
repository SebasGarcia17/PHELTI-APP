package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingJpaRepository extends JpaRepository<RatingEntity, Integer> {

    boolean existsByActivityActivityIdAndRaterUserId(Integer activityId, Integer raterId);

    long countByRatedUserId(Integer userId);

    @Query("SELECT AVG(r.score) FROM RatingEntity r WHERE r.rated.userId = :userId")
    Double findAverageScoreByRatedUserId(@Param("userId") Integer userId);

    @Query("""
            SELECT r FROM RatingEntity r
            JOIN FETCH r.activity
            JOIN FETCH r.rater
            JOIN FETCH r.rated
            WHERE r.rated.userId = :userId
            ORDER BY r.createdAt DESC, r.ratingId DESC
            """)
    List<RatingEntity> findByRatedUserId(@Param("userId") Integer userId);

    @Query("""
            SELECT r FROM RatingEntity r
            JOIN FETCH r.activity
            JOIN FETCH r.rater
            JOIN FETCH r.rated
            WHERE r.activity.activityId = :activityId
            ORDER BY r.ratingId
            """)
    List<RatingEntity> findByActivityId(@Param("activityId") Integer activityId);
}
