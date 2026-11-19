package com.phelti.phelti.model;

import java.math.BigDecimal;

public record PublicationFilter(
        String publicationType,
        String interactionMode,
        Integer categoryId,
        String keyword,
        BigDecimal maxPheltAmount,
        String publicationStatus
) {
}
