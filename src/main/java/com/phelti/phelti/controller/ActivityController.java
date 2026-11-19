package com.phelti.phelti.controller;

import com.phelti.phelti.dto.ActivityRequest;
import com.phelti.phelti.dto.ActivityResponse;
import com.phelti.phelti.dto.ConfirmationRequest;
import com.phelti.phelti.dto.ConfirmationResponse;
import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.ConfirmationEntity;
import com.phelti.phelti.service.ActivityService;
import com.phelti.phelti.service.ConfirmationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities", description = "Scheduling, confirmation and cancellation of time-exchange activities")
public class ActivityController {

    private final ActivityService activityService;
    private final ConfirmationService confirmationService;

    public ActivityController(
            ActivityService activityService,
            ConfirmationService confirmationService) {

        this.activityService = activityService;
        this.confirmationService = confirmationService;
    }

    @Operation(
            summary = "Schedule an activity from an accepted proposal",
            description = "Reserves the agreed phelts from the requester wallet and generates the check-in code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Activity scheduled"),
            @ApiResponse(responseCode = "400", description = "Invalid schedule data"),
            @ApiResponse(responseCode = "404", description = "Proposal, publication, user or wallet not found"),
            @ApiResponse(responseCode = "409", description = "Proposal not accepted, already scheduled or insufficient balance")
    })
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {

        ActivityEntity activity = activityService.createActivity(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(activity));
    }

    @Operation(summary = "Get an activity by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity found"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> findActivityById(
            @Parameter(description = "Activity identifier", example = "1")
            @PathVariable("id") Integer activityId) {

        return ResponseEntity.ok(toResponse(activityService.findActivityById(activityId)));
    }

    @Operation(
            summary = "List the activities of a user",
            description = "Returns the activities where the user is the requester or the participant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activities listed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ActivityResponse>> findActivitiesByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        List<ActivityResponse> activities = activityService.findActivitiesByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(activities);
    }

    @Operation(
            summary = "Confirm that an activity took place",
            description = "Each of the two users confirms with the check-in code. When both have confirmed, the reserved phelts are transferred to the participant, the publication is completed and the activity becomes COMPLETED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmation registered, the activity is returned with its current status"),
            @ApiResponse(responseCode = "400", description = "Check-in code does not match or the user is not part of the activity"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "409", description = "Activity not scheduled or the user already confirmed")
    })
    @PostMapping("/{id}/confirmations")
    public ResponseEntity<ActivityResponse> confirmActivity(
            @Parameter(description = "Activity identifier", example = "1")
            @PathVariable("id") Integer activityId,
            @Valid @RequestBody ConfirmationRequest request) {

        ActivityEntity activity = confirmationService.confirmActivity(
                activityId,
                request.userId(),
                request.checkInCode()
        );

        return ResponseEntity.ok(toResponse(activity));
    }

    @Operation(summary = "List the confirmations of an activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmations listed"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/{id}/confirmations")
    public ResponseEntity<List<ConfirmationResponse>> findConfirmations(
            @Parameter(description = "Activity identifier", example = "1")
            @PathVariable("id") Integer activityId) {

        List<ConfirmationResponse> confirmations = confirmationService
                .findConfirmationsByActivityId(activityId)
                .stream()
                .map(this::toConfirmationResponse)
                .toList();

        return ResponseEntity.ok(confirmations);
    }

    @Operation(
            summary = "Cancel an activity",
            description = "Releases the reserved phelts back to the requester, marks the proposal as CANCELLED and republishes the publication"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity cancelled"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "409", description = "Only scheduled activities can be cancelled")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ActivityResponse> cancelActivity(
            @Parameter(description = "Activity identifier", example = "1")
            @PathVariable("id") Integer activityId) {

        return ResponseEntity.ok(toResponse(activityService.cancelActivity(activityId)));
    }

    private ConfirmationResponse toConfirmationResponse(ConfirmationEntity confirmation) {

        return new ConfirmationResponse(
                confirmation.getConfirmationId(),
                confirmation.getActivity().getActivityId(),
                confirmation.getUser().getUserId(),
                confirmation.getConfirmedAt()
        );
    }

    private ActivityResponse toResponse(ActivityEntity activity) {

        return new ActivityResponse(
                activity.getActivityId(),
                activity.getPublicationId(),
                activity.getProposalId(),
                activity.getRequester().getUserId(),
                activity.getParticipant().getUserId(),
                activity.getInteractionMode(),
                activity.getActivityDate(),
                activity.getActivityTime(),
                activity.getDurationMinutes(),
                activity.getLocation(),
                activity.getVirtualLink(),
                activity.getAgreedAmount(),
                activity.getCheckInCode(),
                activity.getActivityStatus()
        );
    }
}
