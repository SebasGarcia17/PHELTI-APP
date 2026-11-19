package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(String name, String email, String description);

    UserEntity findUserById(Integer userId);

    List<UserEntity> findAllUsers();

    UserEntity updateUserProfile(
            Integer userId,
            String name,
            String description,
            String location,
            String preferredInteractionMode);

    List<UserEntity> searchUsers(
            Integer skillId,
            Integer interestId,
            String interactionMode,
            String dayOfWeek);
}
