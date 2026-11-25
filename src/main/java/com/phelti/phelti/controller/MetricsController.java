package com.phelti.phelti.controller;

import com.phelti.phelti.config.DemoAccess;
import com.phelti.phelti.dto.UserMetricsResponse;
import com.phelti.phelti.model.UserMetrics;
import com.phelti.phelti.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Metrics", description = "Compliance and reputation metrics of the users")
public class MetricsController {

    private final MetricsService metricsService;
    private final DemoAccess access;

    public MetricsController(MetricsService metricsService, DemoAccess access) {
        this.metricsService = metricsService;
        this.access = access;
    }

    @Operation(
            summary = "Get the compliance and reputation metrics of a user",
            description = "Counts activities by status, completion rate, average rating, ratings received and phelts earned and spent"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics calculated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}/metrics")
    public ResponseEntity<UserMetricsResponse> findUserMetrics(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable("id") Integer userId) {

        access.requireGateway();
        return ResponseEntity.ok(toResponse(metricsService.findUserMetrics(userId)));
    }

    private UserMetricsResponse toResponse(UserMetrics metrics) {

        return new UserMetricsResponse(
                metrics.userId(),
                metrics.totalActivities(),
                metrics.scheduledActivities(),
                metrics.completedActivities(),
                metrics.cancelledActivities(),
                metrics.completionRate(),
                metrics.averageRating(),
                metrics.ratingsReceived(),
                metrics.pheltsEarned(),
                metrics.pheltsSpent()
        );
    }
}
