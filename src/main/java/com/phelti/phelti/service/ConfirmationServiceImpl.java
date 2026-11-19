package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.ConfirmationEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.ConfirmationAlreadyExistsException;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidCheckInCodeException;
import com.phelti.phelti.exception.InvalidConfirmationException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.repository.jpa.ConfirmationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    private static final int REQUIRED_CONFIRMATIONS = 2;

    private final ConfirmationJpaRepository confirmationRepository;
    private final ActivityService activityService;

    public ConfirmationServiceImpl(
            ConfirmationJpaRepository confirmationRepository,
            ActivityService activityService) {

        this.confirmationRepository = confirmationRepository;
        this.activityService = activityService;
    }

    @Override
    @Transactional
    public ActivityEntity confirmActivity(
            Integer activityId,
            Integer userId,
            String checkInCode) {

        if (userId == null) {
            throw new InvalidConfirmationException("userId is required");
        }

        ActivityEntity activity = activityService.findActivityById(activityId);

        if (!ActivityStatus.SCHEDULED.name().equals(activity.getActivityStatus())) {
            throw new InvalidActivityStateException(
                    "only scheduled activities can be confirmed"
            );
        }

        UserEntity confirmingUser = findActivityUser(activity, userId);

        if (checkInCode == null || !checkInCode.equals(activity.getCheckInCode())) {
            throw new InvalidCheckInCodeException("check-in code does not match");
        }

        if (confirmationRepository.existsByActivityActivityIdAndUserUserId(activityId, userId)) {
            throw new ConfirmationAlreadyExistsException(userId);
        }

        ConfirmationEntity confirmation = new ConfirmationEntity();
        confirmation.setActivity(activity);
        confirmation.setUser(confirmingUser);
        confirmation.setConfirmedAt(LocalDateTime.now());

        confirmationRepository.save(confirmation);

        if (confirmationRepository.countByActivityActivityId(activityId)
                >= REQUIRED_CONFIRMATIONS) {

            return activityService.completeActivity(activityId, activity.getCheckInCode());
        }

        return activity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfirmationEntity> findConfirmationsByActivityId(Integer activityId) {

        activityService.findActivityById(activityId);

        return confirmationRepository.findByActivityId(activityId);
    }

    private UserEntity findActivityUser(ActivityEntity activity, Integer userId) {

        if (userId.equals(activity.getRequester().getUserId())) {
            return activity.getRequester();
        }

        if (userId.equals(activity.getParticipant().getUserId())) {
            return activity.getParticipant();
        }

        throw new InvalidConfirmationException(
                "only the requester or the participant can confirm the activity"
        );
    }
}
