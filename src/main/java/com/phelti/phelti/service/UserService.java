package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(String name, String email, String description);

    UserEntity findUserById(Integer userId);

    List<UserEntity> findAllUsers();
}
