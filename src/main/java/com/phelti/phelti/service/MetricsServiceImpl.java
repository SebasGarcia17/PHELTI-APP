package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.model.UserMetrics;
import com.phelti.phelti.repository.jpa.RatingJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MetricsServiceImpl implements MetricsService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final UserService userService;
    private final ActivityService activityService;
    private final RatingJpaRepository ratingRepository;

    public MetricsServiceImpl(
            UserService userService,
            ActivityService activityService,
            RatingJpaRepository ratingRepository) {

        this.userService = userService;
        this.activityService = activityService;
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserMetrics findUserMetrics(Integer userId) {

        UserEntity user = userService.findUserById(userId);
        List<ActivityEntity> activities = activityService.findActivitiesByUserId(userId);

        long scheduled = countByStatus(activities, ActivityStatus.SCHEDULED);
        long completed = countByStatus(activities, ActivityStatus.COMPLETED);
        long cancelled = countByStatus(activities, ActivityStatus.CANCELLED);

        BigDecimal earned = sumCompleted(activities, userId, true);
        BigDecimal spent = sumCompleted(activities, userId, false);

        BigDecimal averageRating = user.getAverageRating() == null
                ? BigDecimal.ZERO
                : user.getAverageRating();

        return new UserMetrics(
                userId,
                activities.size(),
                scheduled,
                completed,
                cancelled,
                completionRate(completed, cancelled),
                averageRating,
                ratingRepository.countByRatedUserId(userId),
                earned,
                spent
        );
    }

    private long countByStatus(List<ActivityEntity> activities, ActivityStatus status) {

        return activities.stream()
                .filter(activity -> status.name().equals(activity.getActivityStatus()))
                .count();
    }

    private BigDecimal completionRate(long completed, long cancelled) {

        long finished = completed + cancelled;

        if (finished == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return BigDecimal.valueOf(completed)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(finished), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumCompleted(
            List<ActivityEntity> activities,
            Integer userId,
            boolean asParticipant) {

        return activities.stream()
                .filter(activity -> ActivityStatus.COMPLETED.name()
                        .equals(activity.getActivityStatus()))
                .filter(activity -> {
                    Integer ownerId = asParticipant
                            ? activity.getParticipant().getUserId()
                            : activity.getRequester().getUserId();
                    return userId.equals(ownerId);
                })
                .map(ActivityEntity::getAgreedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
