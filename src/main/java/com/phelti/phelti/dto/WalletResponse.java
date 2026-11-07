package com.phelti.phelti.dto;

import java.math.BigDecimal;

public class WalletResponse {

    private Integer walletId;
    private Integer userId;
    private BigDecimal availableBalance;
    private BigDecimal reservedBalance;

    public WalletResponse(
            Integer walletId,
            Integer userId,
            BigDecimal availableBalance,
            BigDecimal reservedBalance) {

        this.walletId = walletId;
        this.userId = userId;
        this.availableBalance = availableBalance;
        this.reservedBalance = reservedBalance;
    }

    public Integer getWalletId() {
        return walletId;
    }

    public Integer getUserId() {
        return userId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getReservedBalance() {
        return reservedBalance;
    }
}