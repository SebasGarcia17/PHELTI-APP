package com.phelti.phelti.service;

import com.phelti.phelti.entity.InterestEntity;

import java.util.List;

public interface InterestService {

    List<InterestEntity> findAllInterests();

    List<InterestEntity> findInterestsByUserId(Integer userId);

    void addInterestToUser(Integer userId, Integer interestId);

    void removeInterestFromUser(Integer userId, Integer interestId);
}
