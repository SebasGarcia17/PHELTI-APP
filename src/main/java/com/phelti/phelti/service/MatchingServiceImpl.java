package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;
import com.phelti.phelti.model.PublicationMatch;
import com.phelti.phelti.model.PublicationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MatchingServiceImpl implements MatchingService {

    private static final int SKILL_POINTS = 3;
    private static final int INTEREST_POINTS = 2;
    private static final int MODE_POINTS = 1;
    private static final int AVAILABILITY_POINTS = 2;

    private final UserService userService;
    private final PublicationService publicationService;
    private final CategoryService categoryService;

    public MatchingServiceImpl(
            UserService userService,
            PublicationService publicationService,
            CategoryService categoryService) {

        this.userService = userService;
        this.publicationService = publicationService;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicationMatch> findMatchesForUser(Integer userId) {

        UserEntity user = userService.findUserById(userId);

        Map<Integer, String> categoryNames = loadCategoryNames();
        Map<Integer, UserEntity> owners = new HashMap<>();

        List<Publication> candidates = publicationService.searchPublications(
                new PublicationFilter(null, null, null, null, null, null)
        );

        List<PublicationMatch> matches = new ArrayList<>();

        for (Publication publication : candidates) {

            if (userId.equals(publication.getUserId())) {
                continue;
            }

            if (!isModeCompatible(user, publication)) {
                continue;
            }

            List<String> reasons = new ArrayList<>();
            int score = contentScore(user, publication, categoryNames, reasons);

            if (score == 0) {
                continue;
            }

            score += modeScore(user, publication, reasons);

            UserEntity owner = owners.computeIfAbsent(
                    publication.getUserId(),
                    userService::findUserById
            );

            score += availabilityScore(user, owner, reasons);

            matches.add(new PublicationMatch(publication, score, reasons));
        }

        matches.sort(
                Comparator.comparingInt(PublicationMatch::score).reversed()
                        .thenComparing(
                                match -> match.publication().getPublicationId(),
                                Comparator.reverseOrder()
                        )
        );

        return matches;
    }

    private Map<Integer, String> loadCategoryNames() {

        Map<Integer, String> names = new HashMap<>();

        for (CategoryEntity category : categoryService.findAllCategories()) {
            names.put(category.getCategoryId(), category.getName().toLowerCase());
        }

        return names;
    }

    private boolean isModeCompatible(UserEntity user, Publication publication) {

        String preferred = user.getPreferredInteractionMode();

        return preferred == null || preferred.equals(publication.getInteractionMode());
    }

    private int contentScore(
            UserEntity user,
            Publication publication,
            Map<Integer, String> categoryNames,
            List<String> reasons) {

        String text = buildSearchableText(publication, categoryNames);

        boolean publicationNeedsSupport =
                PublicationType.NEEDS_SUPPORT.name().equals(publication.getPublicationType());

        int score = 0;

        if (publicationNeedsSupport) {
            for (SkillEntity skill : user.getSkills()) {
                if (appearsIn(text, skill.getName())) {
                    score += SKILL_POINTS;
                    reasons.add("you can help with: " + skill.getName());
                }
            }
        } else {
            for (InterestEntity interest : user.getInterests()) {
                if (appearsIn(text, interest.getName())) {
                    score += INTEREST_POINTS;
                    reasons.add("matches your interest: " + interest.getName());
                }
            }
        }

        return score;
    }

    private int modeScore(UserEntity user, Publication publication, List<String> reasons) {

        if (user.getPreferredInteractionMode() == null) {
            return 0;
        }

        reasons.add("matches your preferred mode: " + publication.getInteractionMode());

        return MODE_POINTS;
    }

    private int availabilityScore(UserEntity user, UserEntity owner, List<String> reasons) {

        Optional<String> sharedDay = findSharedAvailabilityDay(user, owner);

        if (sharedDay.isEmpty()) {
            return 0;
        }

        reasons.add("both have availability on " + sharedDay.get());

        return AVAILABILITY_POINTS;
    }

    private Optional<String> findSharedAvailabilityDay(UserEntity user, UserEntity owner) {

        for (AvailabilityEntity mine : user.getAvailabilities()) {
            for (AvailabilityEntity theirs : owner.getAvailabilities()) {

                boolean sameDay = mine.getDayOfWeek().equals(theirs.getDayOfWeek());

                boolean overlap = mine.getStartTime().isBefore(theirs.getEndTime())
                        && mine.getEndTime().isAfter(theirs.getStartTime());

                if (sameDay && overlap) {
                    return Optional.of(mine.getDayOfWeek());
                }
            }
        }

        return Optional.empty();
    }

    private String buildSearchableText(
            Publication publication,
            Map<Integer, String> categoryNames) {

        StringBuilder text = new StringBuilder();

        text.append(nullToEmpty(publication.getTitle())).append(' ');
        text.append(nullToEmpty(publication.getDescription())).append(' ');
        text.append(categoryNames.getOrDefault(publication.getCategoryId(), ""));

        return normalize(text.toString());
    }

    private boolean appearsIn(String text, String term) {

        return term != null && !term.isBlank() && text.contains(normalize(term));
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT);
    }

    private String nullToEmpty(String value) {

        return value == null ? "" : value;
    }
}
