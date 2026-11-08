package com.phelti.phelti.service;

import com.phelti.phelti.dto.ActivityRequest;
import com.phelti.phelti.entity.ActivityEntity;

import java.util.List;

public interface ActivityService {

    ActivityEntity createActivity(ActivityRequest request);

    ActivityEntity findActivityById(Integer activityId);

    List<ActivityEntity> findActivitiesByUserId(Integer userId);

    ActivityEntity completeActivity(Integer activityId, String checkInCode);

    ActivityEntity cancelActivity(Integer activityId);
}
