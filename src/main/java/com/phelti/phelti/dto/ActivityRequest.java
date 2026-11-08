package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Data required to schedule an activity from an accepted proposal")
public record ActivityRequest(
        @Schema(description = "Accepted proposal that originates the activity", example = "1")
        @NotNull
        Integer proposalId,
        @Schema(description = "Day of the activity, not in the past", example = "2026-11-20")
        @NotNull
        @FutureOrPresent
        LocalDate activityDate,
        @Schema(description = "Start time", example = "15:30:00")
        @NotNull
        LocalTime activityTime,
        @Schema(description = "Duration in minutes, greater than zero", example = "60")
        @NotNull
        @Positive
        Integer durationMinutes,
        @Schema(description = "Place of the activity, required when the publication is PRESENTIAL (max 200 characters)", example = "Biblioteca Central")
        @Size(max = 200)
        String location,
        @Schema(description = "Meeting link, required when the publication is VIRTUAL (max 300 characters)", example = "https://meet.example.com/abc")
        @Size(max = 300)
        String virtualLink
) {
}
