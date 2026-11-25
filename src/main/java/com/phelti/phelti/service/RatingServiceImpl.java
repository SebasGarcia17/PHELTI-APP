package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.RatingEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidRatingDataException;
import com.phelti.phelti.exception.RatingAlreadyExistsException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.repository.jpa.RatingJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_COMMENT_LENGTH = 500;

    private final RatingJpaRepository ratingRepository;
    private final UserJpaRepository userRepository;
    private final ActivityService activityService;

    public RatingServiceImpl(
            RatingJpaRepository ratingRepository,
            UserJpaRepository userRepository,
            ActivityService activityService) {

        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @Override
    @Transactional
    public RatingEntity createRating(
            Integer activityId,
            Integer raterId,
            Integer score,
            String comment) {

        validateRatingData(raterId, score, comment);

        ActivityEntity activity = activityService.findActivityById(activityId);

        if (!ActivityStatus.COMPLETED.name().equals(activity.getActivityStatus())) {
            throw new InvalidActivityStateException(
                    "Solo puedes calificar actividades completadas."
            );
        }

        UserEntity rater;
        UserEntity rated;

        if (raterId.equals(activity.getRequester().getUserId())) {
            rater = activity.getRequester();
            rated = activity.getParticipant();
        } else if (raterId.equals(activity.getParticipant().getUserId())) {
            rater = activity.getParticipant();
            rated = activity.getRequester();
        } else {
            throw new InvalidRatingDataException(
                    "Solo los participantes pueden calificar la actividad."
            );
        }

        userRepository.lockParticipants(List.of(rated.getUserId()));
        if (ratingRepository.existsByActivityActivityIdAndRaterUserId(activityId, raterId)) {
            throw new RatingAlreadyExistsException(raterId);
        }

        RatingEntity rating = new RatingEntity();
        rating.setActivity(activity);
        rating.setRater(rater);
        rating.setRated(rated);
        rating.setScore(score);
        rating.setComment(comment);
        rating.setCreatedAt(com.phelti.phelti.model.BusinessRules.now());

        RatingEntity saved = ratingRepository.save(rating);

        updateAverageRating(rated);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingEntity> findRatingsByUserId(Integer userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return ratingRepository.findByRatedUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingEntity> findRatingsByActivityId(Integer activityId) {

        activityService.findActivityById(activityId);

        return ratingRepository.findByActivityId(activityId);
    }

    private void updateAverageRating(UserEntity rated) {

        Double average = ratingRepository.findAverageScoreByRatedUserId(rated.getUserId());

        if (average != null) {
            rated.setAverageRating(
                    BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP)
            );
            userRepository.save(rated);
        }
    }

    private void validateRatingData(Integer raterId, Integer score, String comment) {

        if (raterId == null) {
            throw new InvalidRatingDataException("Identifica a la persona que califica.");
        }

        if (score == null || score < MIN_SCORE || score > MAX_SCORE) {
            throw new InvalidRatingDataException(
                    "La puntuación debe estar entre " + MIN_SCORE + " y " + MAX_SCORE + "."
            );
        }

        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidRatingDataException(
                    "El comentario admite hasta " + MAX_COMMENT_LENGTH + " caracteres."
            );
        }
    }
}
