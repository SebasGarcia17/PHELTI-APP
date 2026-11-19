package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.RatingEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.ActivityNotFoundException;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidRatingDataException;
import com.phelti.phelti.exception.RatingAlreadyExistsException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.repository.jpa.RatingJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingJpaRepository ratingRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    void createRatingShouldRateTheParticipantWhenTheRequesterRates() {

        ActivityEntity activity = createActivity(ActivityStatus.COMPLETED);

        stubSave(activity);

        when(ratingRepository.findAverageScoreByRatedUserId(1))
                .thenReturn(4.0);

        RatingEntity result = ratingService.createRating(1, 2, 4, "Great help");

        assertEquals(activity.getRequester(), result.getRater());
        assertEquals(activity.getParticipant(), result.getRated());
        assertEquals(4, result.getScore());
        assertEquals("Great help", result.getComment());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createRatingShouldRateTheRequesterWhenTheParticipantRates() {

        ActivityEntity activity = createActivity(ActivityStatus.COMPLETED);

        stubSave(activity);

        when(ratingRepository.findAverageScoreByRatedUserId(2))
                .thenReturn(5.0);

        RatingEntity result = ratingService.createRating(1, 1, 5, null);

        assertEquals(activity.getParticipant(), result.getRater());
        assertEquals(activity.getRequester(), result.getRated());
    }

    @Test
    void createRatingShouldUpdateTheAverageRatingOfTheRatedUser() {

        ActivityEntity activity = createActivity(ActivityStatus.COMPLETED);

        stubSave(activity);

        when(ratingRepository.findAverageScoreByRatedUserId(1))
                .thenReturn(4.3333333);

        ratingService.createRating(1, 2, 4, null);

        UserEntity rated = activity.getParticipant();

        assertEquals(0, new BigDecimal("4.33").compareTo(rated.getAverageRating()));
        verify(userRepository).save(rated);
    }

    @Test
    void createRatingShouldThrowExceptionWhenScoreIsOutOfRange() {

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, 2, 6, null)
        );

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, 2, 0, null)
        );

        verify(ratingRepository, never()).save(any(RatingEntity.class));
    }

    @Test
    void createRatingShouldThrowExceptionWhenScoreIsMissing() {

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, 2, null, null)
        );
    }

    @Test
    void createRatingShouldThrowExceptionWhenRaterIsMissing() {

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, null, 5, null)
        );
    }

    @Test
    void createRatingShouldThrowExceptionWhenCommentIsTooLong() {

        String longComment = "x".repeat(501);

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, 2, 5, longComment)
        );
    }

    @Test
    void createRatingShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityService.findActivityById(9))
                .thenThrow(new ActivityNotFoundException(9));

        assertThrows(
                ActivityNotFoundException.class,
                () -> ratingService.createRating(9, 2, 5, null)
        );
    }

    @Test
    void createRatingShouldThrowExceptionWhenActivityIsNotCompleted() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        assertThrows(
                InvalidActivityStateException.class,
                () -> ratingService.createRating(1, 2, 5, null)
        );

        verify(ratingRepository, never()).save(any(RatingEntity.class));
    }

    @Test
    void createRatingShouldThrowExceptionWhenRaterIsNotPartOfTheActivity() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.COMPLETED));

        assertThrows(
                InvalidRatingDataException.class,
                () -> ratingService.createRating(1, 99, 5, null)
        );

        verify(ratingRepository, never()).save(any(RatingEntity.class));
    }

    @Test
    void createRatingShouldThrowExceptionWhenUserAlreadyRatedTheActivity() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.COMPLETED));

        when(ratingRepository.existsByActivityActivityIdAndRaterUserId(1, 2))
                .thenReturn(true);

        assertThrows(
                RatingAlreadyExistsException.class,
                () -> ratingService.createRating(1, 2, 5, null)
        );

        verify(ratingRepository, never()).save(any(RatingEntity.class));
    }

    @Test
    void findRatingsByUserIdShouldReturnTheRatingsReceived() {

        List<RatingEntity> ratings = List.of(new RatingEntity());

        when(userRepository.existsById(1))
                .thenReturn(true);

        when(ratingRepository.findByRatedUserId(1))
                .thenReturn(ratings);

        assertEquals(ratings, ratingService.findRatingsByUserId(1));
    }

    @Test
    void findRatingsByUserIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.existsById(9))
                .thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> ratingService.findRatingsByUserId(9)
        );

        verify(ratingRepository, never()).findByRatedUserId(any());
    }

    @Test
    void findRatingsByActivityIdShouldReturnTheRatingsOfTheActivity() {

        List<RatingEntity> ratings = List.of(new RatingEntity(), new RatingEntity());

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.COMPLETED));

        when(ratingRepository.findByActivityId(1))
                .thenReturn(ratings);

        assertEquals(ratings, ratingService.findRatingsByActivityId(1));
    }

    @Test
    void findRatingsByActivityIdShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityService.findActivityById(9))
                .thenThrow(new ActivityNotFoundException(9));

        assertThrows(
                ActivityNotFoundException.class,
                () -> ratingService.findRatingsByActivityId(9)
        );
    }

    private void stubSave(ActivityEntity activity) {

        when(activityService.findActivityById(1))
                .thenReturn(activity);

        when(ratingRepository.save(any(RatingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ActivityEntity createActivity(ActivityStatus status) {

        UserEntity requester = new UserEntity();
        requester.setUserId(2);
        requester.setAverageRating(BigDecimal.ZERO);

        UserEntity participant = new UserEntity();
        participant.setUserId(1);
        participant.setAverageRating(BigDecimal.ZERO);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityId(1);
        activity.setRequester(requester);
        activity.setParticipant(participant);
        activity.setActivityStatus(status.name());

        return activity;
    }
}
