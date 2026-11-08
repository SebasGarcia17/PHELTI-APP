package com.phelti.phelti.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Wallet of a user with its current phelt balances")
public record WalletResponse(
        @Schema(description = "Wallet identifier", example = "1")
        Integer walletId,
        @Schema(description = "Owner of the wallet", example = "1")
        Integer userId,
        @Schema(description = "Phelts available to spend", example = "80.00")
        BigDecimal availableBalance,
        @Schema(description = "Phelts reserved for scheduled activities", example = "20.00")
        BigDecimal reservedBalance
) {
}
