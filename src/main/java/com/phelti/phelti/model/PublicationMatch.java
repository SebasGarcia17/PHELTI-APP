package com.phelti.phelti.model;

import java.util.List;

public record PublicationMatch(
        Publication publication,
        int score,
        List<String> reasons
) {
}
