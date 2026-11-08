package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Scheduled time-exchange activity")
public record ActivityResponse(
        @Schema(description = "Activity identifier", example = "1")
        Integer activityId,
        @Schema(description = "Publication the activity belongs to", example = "1")
        Integer publicationId,
        @Schema(description = "Proposal that originated the activity", example = "1")
        Integer proposalId,
        @Schema(description = "User who receives the service and pays the phelts", example = "2")
        Integer requesterId,
        @Schema(description = "User who provides the service and receives the phelts", example = "1")
        Integer participantId,
        @Schema(description = "Interaction mode", allowableValues = {"VIRTUAL", "PRESENTIAL"})
        String interactionMode,
        @Schema(description = "Day of the activity", example = "2026-11-20")
        LocalDate activityDate,
        @Schema(description = "Start time", example = "15:30:00")
        LocalTime activityTime,
        @Schema(description = "Duration in minutes", example = "60")
        Integer durationMinutes,
        @Schema(description = "Place of the activity")
        String location,
        @Schema(description = "Meeting link")
        String virtualLink,
        @Schema(description = "Phelts agreed for the activity", example = "20.00")
        BigDecimal agreedAmount,
        @Schema(description = "Code needed to complete the activity")
        String checkInCode,
        @Schema(description = "Activity status", allowableValues = {"SCHEDULED", "COMPLETED", "CANCELLED"})
        String activityStatus
) {
}
