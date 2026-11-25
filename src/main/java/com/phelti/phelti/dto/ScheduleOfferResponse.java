package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record ScheduleOfferResponse(Integer scheduleId, Integer proposalId, Integer requesterId,
        LocalDate activityDate, LocalTime activityTime, Integer durationMinutes, String location,
        String virtualLink,
        @Schema(allowableValues = {"PENDING", "ACCEPTED", "REJECTED", "WITHDRAWN"}) String status,
        LocalDateTime createdAt, LocalDateTime decidedAt) {
    public ActivityRequest conditions() {
        return new ActivityRequest(proposalId, activityDate, activityTime, durationMinutes, location, virtualLink);
    }
}
