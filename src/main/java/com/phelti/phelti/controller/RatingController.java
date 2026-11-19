package com.phelti.phelti.controller;

import com.phelti.phelti.dto.RatingRequest;
import com.phelti.phelti.dto.RatingResponse;
import com.phelti.phelti.entity.RatingEntity;
import com.phelti.phelti.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Ratings", description = "Ratings of completed activities that build the reputation of the users")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(
            summary = "Rate a completed activity",
            description = "One of the two users rates the other with a score from 1 to 5 and an optional comment. Each user can rate an activity once and the average rating of the rated user is updated"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rating created"),
            @ApiResponse(responseCode = "400", description = "Invalid rating data or the user is not part of the activity"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "409", description = "Activity not completed or already rated by the user")
    })
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(
            @Valid @RequestBody RatingRequest request) {

        RatingEntity rating = ratingService.createRating(
                request.activityId(),
                request.raterId(),
                request.score(),
                request.comment()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(rating));
    }

    @Operation(summary = "List the ratings received by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ratings listed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<RatingResponse>> findRatingsByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        return ResponseEntity.ok(toResponses(ratingService.findRatingsByUserId(userId)));
    }

    @Operation(summary = "List the ratings of an activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ratings listed"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/activities/{activityId}")
    public ResponseEntity<List<RatingResponse>> findRatingsByActivityId(
            @Parameter(description = "Activity identifier", example = "1")
            @PathVariable Integer activityId) {

        return ResponseEntity.ok(toResponses(ratingService.findRatingsByActivityId(activityId)));
    }

    private List<RatingResponse> toResponses(List<RatingEntity> ratings) {

        return ratings.stream()
                .map(this::toResponse)
                .toList();
    }

    private RatingResponse toResponse(RatingEntity rating) {

        return new RatingResponse(
                rating.getRatingId(),
                rating.getActivity().getActivityId(),
                rating.getRater().getUserId(),
                rating.getRated().getUserId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}
