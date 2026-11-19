package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.model.UserMetrics;
import com.phelti.phelti.repository.jpa.RatingJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ActivityService activityService;

    @Mock
    private RatingJpaRepository ratingRepository;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @Test
    void findUserMetricsShouldCountActivitiesByStatusAndCalculateCompletionRate() {

        UserEntity user = createUser(1, "4.50");

        List<ActivityEntity> activities = List.of(
                createActivity(1, 1, 2, ActivityStatus.COMPLETED, "20.00"),
                createActivity(2, 1, 3, ActivityStatus.COMPLETED, "10.00"),
                createActivity(3, 2, 1, ActivityStatus.COMPLETED, "5.00"),
                createActivity(4, 1, 2, ActivityStatus.CANCELLED, "8.00"),
                createActivity(5, 1, 2, ActivityStatus.SCHEDULED, "7.00")
        );

        when(userService.findUserById(1))
                .thenReturn(user);

        when(activityService.findActivitiesByUserId(1))
                .thenReturn(activities);

        when(ratingRepository.countByRatedUserId(1))
                .thenReturn(3L);

        UserMetrics metrics = metricsService.findUserMetrics(1);

        assertEquals(1, metrics.userId());
        assertEquals(5, metrics.totalActivities());
        assertEquals(1, metrics.scheduledActivities());
        assertEquals(3, metrics.completedActivities());
        assertEquals(1, metrics.cancelledActivities());
        assertEquals(0, new BigDecimal("75.00").compareTo(metrics.completionRate()));
        assertEquals(0, new BigDecimal("4.50").compareTo(metrics.averageRating()));
        assertEquals(3, metrics.ratingsReceived());
        assertEquals(0, new BigDecimal("30.00").compareTo(metrics.pheltsEarned()));
        assertEquals(0, new BigDecimal("5.00").compareTo(metrics.pheltsSpent()));
    }

    @Test
    void findUserMetricsShouldReturnZeroesWhenTheUserHasNoActivities() {

        when(userService.findUserById(1))
                .thenReturn(createUser(1, null));

        when(activityService.findActivitiesByUserId(1))
                .thenReturn(List.of());

        when(ratingRepository.countByRatedUserId(1))
                .thenReturn(0L);

        UserMetrics metrics = metricsService.findUserMetrics(1);

        assertEquals(0, metrics.totalActivities());
        assertEquals(0, BigDecimal.ZERO.compareTo(metrics.completionRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(metrics.averageRating()));
        assertEquals(0, BigDecimal.ZERO.compareTo(metrics.pheltsEarned()));
        assertEquals(0, BigDecimal.ZERO.compareTo(metrics.pheltsSpent()));
    }

    @Test
    void findUserMetricsShouldReturnZeroCompletionRateWhenNothingIsFinished() {

        when(userService.findUserById(1))
                .thenReturn(createUser(1, "0.00"));

        when(activityService.findActivitiesByUserId(1))
                .thenReturn(List.of(createActivity(1, 1, 2, ActivityStatus.SCHEDULED, "10.00")));

        when(ratingRepository.countByRatedUserId(1))
                .thenReturn(0L);

        UserMetrics metrics = metricsService.findUserMetrics(1);

        assertEquals(0, BigDecimal.ZERO.compareTo(metrics.completionRate()));
        assertEquals(1, metrics.scheduledActivities());
    }

    @Test
    void findUserMetricsShouldThrowExceptionWhenUserDoesNotExist() {

        when(userService.findUserById(9))
                .thenThrow(new UserNotFoundException(9));

        assertThrows(
                UserNotFoundException.class,
                () -> metricsService.findUserMetrics(9)
        );

        verify(activityService, never()).findActivitiesByUserId(9);
    }

    private UserEntity createUser(Integer userId, String averageRating) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setAverageRating(averageRating == null ? null : new BigDecimal(averageRating));

        return user;
    }

    private ActivityEntity createActivity(
            Integer id,
            Integer participantId,
            Integer requesterId,
            ActivityStatus status,
            String amount) {

        UserEntity participant = new UserEntity();
        participant.setUserId(participantId);

        UserEntity requester = new UserEntity();
        requester.setUserId(requesterId);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityId(id);
        activity.setParticipant(participant);
        activity.setRequester(requester);
        activity.setActivityStatus(status.name());
        activity.setAgreedAmount(new BigDecimal(amount));

        return activity;
    }
}
