package com.phelti.phelti.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class BusinessRules {
    public static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");
    private BusinessRules() { }
    public static LocalDateTime now() { return LocalDateTime.now(ZONE); }
    public static boolean validBalance(BigDecimal amount) { return amount != null && amount.signum() >= 0 && amount.compareTo(MAX_AMOUNT) <= 0; }
    public static boolean validAmount(BigDecimal amount) {
        return amount != null && amount.signum() > 0 && amount.compareTo(MAX_AMOUNT) <= 0
                && amount.stripTrailingZeros().scale() <= 2;
    }
}
