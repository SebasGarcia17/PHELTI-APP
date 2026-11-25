package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM UserEntity u WHERE u.userId IN :ids ORDER BY u.userId")
    java.util.List<UserEntity> lockParticipants(@org.springframework.data.repository.query.Param("ids") java.util.List<Integer> ids);
}
