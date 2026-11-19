package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Movement of phelts in a wallet")
public record TransactionResponse(
        @Schema(description = "Transaction identifier", example = "1")
        Integer transactionId,
        @Schema(description = "Type of movement", allowableValues = {"DEPOSIT", "RESERVE", "RELEASE", "TRANSFER_OUT", "TRANSFER_IN"})
        String transactionType,
        @Schema(description = "Phelts moved", example = "20.00")
        BigDecimal amount,
        @Schema(description = "What the movement was for")
        String description,
        @Schema(description = "Moment of the movement")
        LocalDateTime createdAt,
        @Schema(description = "Activity that originated the movement (null for deposits)", example = "1")
        Integer activityId,
        @Schema(description = "User that pays, for transfers (null otherwise)", example = "2")
        Integer originUserId,
        @Schema(description = "User that receives, for transfers (null otherwise)", example = "1")
        Integer destinationUserId
) {
}
