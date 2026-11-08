package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Phelts to add to the available balance of a wallet")
public record DepositRequest(
        @Schema(description = "Amount to deposit, greater than zero", example = "50.00")
        @NotNull
        @Positive
        BigDecimal amount
) {
}
