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

    private final com.phelti.phelti.config.DemoAccess access;

    private final ActivityService activityService;
    private final com.phelti.phelti.service.ScheduleService schedules;
    private final ConfirmationService confirmationService;

    public ActivityController(
            com.phelti.phelti.config.DemoAccess access, ActivityService activityService,
            ConfirmationService confirmationService, com.phelti.phelti.service.ScheduleService schedules) {
        this.access = access;

        this.activityService = activityService;
        this.schedules = schedules;
        this.confirmationService = confirmationService;
    }

    @Operation(
            summary = "Schedule an activity from an accepted proposal",
            description = "The provider must accept the exact pending schedule proposed by the requester. Reserves phelts atomically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Activity scheduled"),
            @ApiResponse(responseCode = "400", description = "Invalid schedule data"),
            @ApiResponse(responseCode = "404", description = "Proposal, publication, user or wallet not found"),
            @ApiResponse(responseCode = "409", description = "Proposal not accepted, already scheduled or insufficient balance")
    })
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {
        access.proposalParticipant(request.proposalId());

        ActivityEntity activity = schedules.acceptMatching(request, access.actor());

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
        access.activityParticipant(activityId);

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
        access.own(userId);

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
        access.own(request.userId()); access.activityParticipant(activityId);

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
        access.activityParticipant(activityId);

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
        access.activityParticipant(activityId);

        return ResponseEntity.ok(toResponse(activityService.cancelActivity(activityId)));
    }

    @GetMapping("/schedule/{proposalId}")
    public ResponseEntity<List<com.phelti.phelti.dto.ScheduleOfferResponse>> scheduleHistory(@PathVariable Integer proposalId) {
        return ResponseEntity.ok(schedules.history(proposalId, access.actor()));
    }

    @PostMapping("/schedule")
    public ResponseEntity<com.phelti.phelti.dto.ScheduleOfferResponse> proposeSchedule(@Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedules.propose(request, access.actor()));
    }

    @PutMapping("/schedule/{id}/accept")
    public ResponseEntity<ActivityResponse> acceptSchedule(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(schedules.accept(id, access.actor())));
    }

    @PutMapping("/schedule/{id}/decline")
    public ResponseEntity<com.phelti.phelti.dto.ScheduleOfferResponse> declineSchedule(@PathVariable Integer id) {
        return ResponseEntity.ok(schedules.decline(id, access.actor()));
    }

    @PutMapping("/schedule/proposals/{proposalId}/cancel")
    public ResponseEntity<Void> cancelAgreement(@PathVariable Integer proposalId) {
        schedules.cancelAgreement(proposalId, access.actor());
        return ResponseEntity.noContent().build();
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
                "SCHEDULED".equals(activity.getActivityStatus()) && !com.phelti.phelti.model.BusinessRules.now().isBefore(java.time.LocalDateTime.of(activity.getActivityDate(), activity.getActivityTime()).plusMinutes(activity.getDurationMinutes())) ? activity.getCheckInCode() : null,
                activity.getActivityStatus()
        );
    }
}
