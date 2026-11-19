package com.phelti.phelti.service;

import com.phelti.phelti.entity.RatingEntity;

import java.util.List;

public interface RatingService {

    RatingEntity createRating(Integer activityId, Integer raterId, Integer score, String comment);

    List<RatingEntity> findRatingsByUserId(Integer userId);

    List<RatingEntity> findRatingsByActivityId(Integer activityId);
}
