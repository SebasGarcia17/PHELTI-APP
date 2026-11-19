package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.ConfirmationEntity;

import java.util.List;

public interface ConfirmationService {

    ActivityEntity confirmActivity(Integer activityId, Integer userId, String checkInCode);

    List<ConfirmationEntity> findConfirmationsByActivityId(Integer activityId);
}
