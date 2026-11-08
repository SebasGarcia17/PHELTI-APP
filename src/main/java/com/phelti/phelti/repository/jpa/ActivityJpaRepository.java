package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.ActivityEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, Integer> {

    @Override
    @EntityGraph(attributePaths = {"requester", "participant"})
    Optional<ActivityEntity> findById(Integer activityId);

    boolean existsByProposalId(Integer proposalId);

    @Query("""
            SELECT a FROM ActivityEntity a
            JOIN FETCH a.requester
            JOIN FETCH a.participant
            WHERE a.requester.userId = :userId
               OR a.participant.userId = :userId
            ORDER BY a.activityId
            """)
    List<ActivityEntity> findByParticipantOrRequesterId(@Param("userId") Integer userId);
}
