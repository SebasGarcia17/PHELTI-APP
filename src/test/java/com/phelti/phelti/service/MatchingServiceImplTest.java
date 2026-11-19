package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;
import com.phelti.phelti.model.PublicationMatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PublicationService publicationService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    @Test
    void findMatchesShouldMatchSkillsWithPublicationsThatNeedSupport() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));

        UserEntity owner = createUser(2);

        Publication matching = publication(10, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar lessons wanted", "Beginner level", 1);
        Publication unrelated = publication(11, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Math tutoring", "Algebra", 1);

        stubUsersAndPublications(user, owner, List.of(matching, unrelated));

        List<PublicationMatch> result = matchingService.findMatchesForUser(1);

        assertEquals(1, result.size());
        assertEquals(matching, result.get(0).publication());
        assertEquals(3, result.get(0).score());
        assertTrue(result.get(0).reasons().contains("you can help with: Guitar"));
    }

    @Test
    void findMatchesShouldMatchInterestsWithPublicationsThatOfferSupport() {

        UserEntity user = createUser(1);
        user.getInterests().add(interest(1, "Photography"));

        UserEntity owner = createUser(2);

        Publication matching = publication(10, 2, "OFFERS_SUPPORT", "VIRTUAL",
                "Photography basics", "Camera settings", 1);

        stubUsersAndPublications(user, owner, List.of(matching));

        List<PublicationMatch> result = matchingService.findMatchesForUser(1);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).score());
        assertTrue(result.get(0).reasons().contains("matches your interest: Photography"));
    }

    @Test
    void findMatchesShouldNotUseSkillsForOffersNorInterestsForRequests() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));
        user.getInterests().add(interest(1, "Photography"));

        Publication offerWithSkillWord = publication(10, 2, "OFFERS_SUPPORT", "VIRTUAL",
                "Guitar for everyone", "", 1);
        Publication requestWithInterestWord = publication(11, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Photography help", "", 1);

        when(userService.findUserById(1)).thenReturn(user);
        when(categoryService.findAllCategories()).thenReturn(List.of());
        when(publicationService.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of(offerWithSkillWord, requestWithInterestWord));

        assertTrue(matchingService.findMatchesForUser(1).isEmpty());
    }

    @Test
    void findMatchesShouldMatchTheNameOfTheCategory() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Tecnologia"));

        UserEntity owner = createUser(2);

        Publication matching = publication(10, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Need help with my laptop", "", 1);

        when(userService.findUserById(1)).thenReturn(user);
        when(userService.findUserById(2)).thenReturn(owner);
        when(categoryService.findAllCategories()).thenReturn(List.of(category(1, "Tecnologia")));
        when(publicationService.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of(matching));

        assertEquals(1, matchingService.findMatchesForUser(1).size());
    }

    @Test
    void findMatchesShouldExcludeThePublicationsOfTheUser() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));

        Publication own = publication(10, 1, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar lessons wanted", "", 1);

        when(userService.findUserById(1)).thenReturn(user);
        when(categoryService.findAllCategories()).thenReturn(List.of());
        when(publicationService.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of(own));

        assertTrue(matchingService.findMatchesForUser(1).isEmpty());
    }

    @Test
    void findMatchesShouldExcludePublicationsWithADifferentPreferredMode() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));
        user.setPreferredInteractionMode("VIRTUAL");

        Publication presential = publication(10, 2, "NEEDS_SUPPORT", "PRESENTIAL",
                "Guitar lessons wanted", "", 1);

        when(userService.findUserById(1)).thenReturn(user);
        when(categoryService.findAllCategories()).thenReturn(List.of());
        when(publicationService.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of(presential));

        assertTrue(matchingService.findMatchesForUser(1).isEmpty());
        verify(userService, never()).findUserById(2);
    }

    @Test
    void findMatchesShouldAddPointsForPreferredModeAndSharedAvailability() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));
        user.setPreferredInteractionMode("VIRTUAL");
        user.getAvailabilities().add(slot("SATURDAY", 9, 12));

        UserEntity owner = createUser(2);
        owner.getAvailabilities().add(slot("SATURDAY", 11, 14));

        Publication matching = publication(10, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar lessons wanted", "", 1);

        stubUsersAndPublications(user, owner, List.of(matching));

        List<PublicationMatch> result = matchingService.findMatchesForUser(1);

        assertEquals(1, result.size());
        assertEquals(3 + 1 + 2, result.get(0).score());
        assertTrue(result.get(0).reasons().contains("matches your preferred mode: VIRTUAL"));
        assertTrue(result.get(0).reasons().contains("both have availability on SATURDAY"));
    }

    @Test
    void findMatchesShouldNotAddAvailabilityPointsWhenSlotsDoNotOverlap() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));
        user.getAvailabilities().add(slot("SATURDAY", 9, 11));

        UserEntity owner = createUser(2);
        owner.getAvailabilities().add(slot("SATURDAY", 11, 14));

        Publication matching = publication(10, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar lessons wanted", "", 1);

        stubUsersAndPublications(user, owner, List.of(matching));

        assertEquals(3, matchingService.findMatchesForUser(1).get(0).score());
    }

    @Test
    void findMatchesShouldSortByScoreDescending() {

        UserEntity user = createUser(1);
        user.getSkills().add(skill(1, "Guitar"));
        user.getSkills().add(skill(2, "Piano"));

        UserEntity owner = createUser(2);

        Publication oneSkill = publication(10, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar lessons wanted", "", 1);
        Publication twoSkills = publication(11, 2, "NEEDS_SUPPORT", "VIRTUAL",
                "Guitar and piano lessons wanted", "", 1);

        stubUsersAndPublications(user, owner, List.of(oneSkill, twoSkills));

        List<PublicationMatch> result = matchingService.findMatchesForUser(1);

        assertEquals(2, result.size());
        assertEquals(twoSkills, result.get(0).publication());
        assertEquals(6, result.get(0).score());
        assertEquals(oneSkill, result.get(1).publication());
    }

    @Test
    void findMatchesShouldThrowExceptionWhenUserDoesNotExist() {

        when(userService.findUserById(9))
                .thenThrow(new UserNotFoundException(9));

        assertThrows(
                UserNotFoundException.class,
                () -> matchingService.findMatchesForUser(9)
        );

        verify(publicationService, never()).searchPublications(any(PublicationFilter.class));
    }

    private void stubUsersAndPublications(
            UserEntity user,
            UserEntity owner,
            List<Publication> publications) {

        when(userService.findUserById(user.getUserId())).thenReturn(user);
        when(userService.findUserById(owner.getUserId())).thenReturn(owner);
        when(categoryService.findAllCategories()).thenReturn(List.of());
        when(publicationService.searchPublications(any(PublicationFilter.class)))
                .thenReturn(publications);
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        return user;
    }

    private SkillEntity skill(Integer id, String name) {

        SkillEntity skill = new SkillEntity();
        skill.setSkillId(id);
        skill.setName(name);

        return skill;
    }

    private InterestEntity interest(Integer id, String name) {

        InterestEntity interest = new InterestEntity();
        interest.setInterestId(id);
        interest.setName(name);

        return interest;
    }

    private CategoryEntity category(Integer id, String name) {

        CategoryEntity category = new CategoryEntity();
        category.setCategoryId(id);
        category.setName(name);

        return category;
    }

    private AvailabilityEntity slot(String day, int startHour, int endHour) {

        AvailabilityEntity slot = new AvailabilityEntity();
        slot.setDayOfWeek(day);
        slot.setStartTime(LocalTime.of(startHour, 0));
        slot.setEndTime(LocalTime.of(endHour, 0));

        return slot;
    }

    private Publication publication(
            Integer id,
            Integer ownerId,
            String type,
            String mode,
            String title,
            String description,
            Integer categoryId) {

        Publication publication = new Publication();
        publication.setPublicationId(id);
        publication.setUserId(ownerId);
        publication.setPublicationType(type);
        publication.setInteractionMode(mode);
        publication.setTitle(title);
        publication.setDescription(description);
        publication.setCategoryId(categoryId);

        return publication;
    }
}
