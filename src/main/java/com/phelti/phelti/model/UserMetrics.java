package com.phelti.phelti.model;

import java.math.BigDecimal;

public record UserMetrics(
        Integer userId,
        long totalActivities,
        long scheduledActivities,
        long completedActivities,
        long cancelledActivities,
        BigDecimal completionRate,
        BigDecimal averageRating,
        long ratingsReceived,
        BigDecimal pheltsEarned,
        BigDecimal pheltsSpent
) {
}
