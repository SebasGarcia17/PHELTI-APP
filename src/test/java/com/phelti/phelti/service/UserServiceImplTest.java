package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.EmailAlreadyRegisteredException;
import com.phelti.phelti.exception.InvalidUserDataException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.UserJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldSaveUserWithZeroRatingWhenDataIsValid() {

        when(userRepository.existsByEmail("ana@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.createUser(
                "Ana Torres",
                "ana@example.com",
                "Guitar teacher"
        );

        assertEquals("Ana Torres", result.getName());
        assertEquals("ana@example.com", result.getEmail());
        assertEquals("Guitar teacher", result.getDescription());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAverageRating()));

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUserShouldThrowExceptionWhenNameIsBlank() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.createUser(" ", "ana@example.com", null)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createUserShouldThrowExceptionWhenNameIsTooLong() {

        String longName = "a".repeat(101);

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.createUser(longName, "ana@example.com", null)
        );
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailIsInvalid() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.createUser("Ana", "not-an-email", null)
        );

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.createUser("Ana", null, null)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailIsAlreadyRegistered() {

        when(userRepository.existsByEmail("ana@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> userService.createUser("Ana", "ana@example.com", null)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void findUserByIdShouldReturnUserWhenItExists() {

        UserEntity user = new UserEntity();
        user.setUserId(1);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        assertEquals(user, userService.findUserById(1));
    }

    @Test
    void findUserByIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(999)
        );
    }

    @Test
    void findAllUsersShouldReturnRepositoryUsers() {

        List<UserEntity> users = List.of(new UserEntity(), new UserEntity());

        when(userRepository.findAll())
                .thenReturn(users);

        assertEquals(users, userService.findAllUsers());
    }

    @Test
    void updateUserProfileShouldUpdateProfileFieldsWhenDataIsValid() {

        UserEntity user = createUser(1, "Ana");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUserProfile(
                1,
                "Ana Torres",
                "Guitar teacher",
                "  Medellin  ",
                "virtual"
        );

        assertEquals("Ana Torres", result.getName());
        assertEquals("Guitar teacher", result.getDescription());
        assertEquals("Medellin", result.getLocation());
        assertEquals("VIRTUAL", result.getPreferredInteractionMode());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfileShouldClearOptionalFieldsWhenTheyAreBlank() {

        UserEntity user = createUser(1, "Ana");
        user.setLocation("Bogota");
        user.setPreferredInteractionMode("PRESENTIAL");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUserProfile(1, "Ana", null, " ", "");

        assertNull(result.getLocation());
        assertNull(result.getPreferredInteractionMode());
    }

    @Test
    void updateUserProfileShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserProfile(9, "Ana", null, null, null)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUserProfileShouldThrowExceptionWhenNameIsBlank() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1, "Ana")));

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.updateUserProfile(1, " ", null, null, null)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUserProfileShouldThrowExceptionWhenInteractionModeIsInvalid() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1, "Ana")));

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.updateUserProfile(1, "Ana", null, null, "TELEPATHIC")
        );
    }

    @Test
    void updateUserProfileShouldThrowExceptionWhenLocationIsTooLong() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1, "Ana")));

        String longLocation = "x".repeat(151);

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.updateUserProfile(1, "Ana", null, longLocation, null)
        );
    }

    @Test
    void searchUsersShouldFilterBySkillInterestModeAndDay() {

        SkillEntity guitar = skill(1, "Guitar");
        InterestEntity photo = interest(1, "Photography");

        UserEntity match = createUser(1, "Match");
        match.getSkills().add(guitar);
        match.getInterests().add(photo);
        match.setPreferredInteractionMode("VIRTUAL");
        match.getAvailabilities().add(slot("SATURDAY"));

        UserEntity onlySkill = createUser(2, "Only skill");
        onlySkill.getSkills().add(guitar);

        UserEntity nothing = createUser(3, "Nothing");

        when(userRepository.findAll())
                .thenReturn(List.of(match, onlySkill, nothing));

        List<UserEntity> result = userService.searchUsers(1, 1, "virtual", "saturday");

        assertEquals(List.of(match), result);
    }

    @Test
    void searchUsersShouldReturnEveryUserWhenNoFilterIsGiven() {

        List<UserEntity> users = List.of(createUser(1, "A"), createUser(2, "B"));

        when(userRepository.findAll())
                .thenReturn(users);

        assertEquals(users, userService.searchUsers(null, null, null, null));
    }

    @Test
    void searchUsersShouldThrowExceptionWhenDayIsInvalid() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.searchUsers(null, null, null, "FUNDAY")
        );

        verify(userRepository, never()).findAll();
    }

    @Test
    void searchUsersShouldThrowExceptionWhenInteractionModeIsInvalid() {

        assertThrows(
                InvalidUserDataException.class,
                () -> userService.searchUsers(null, null, "TELEPATHIC", null)
        );
    }

    private UserEntity createUser(Integer userId, String name) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail("user" + userId + "@example.com");

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

    private AvailabilityEntity slot(String day) {

        AvailabilityEntity slot = new AvailabilityEntity();
        slot.setDayOfWeek(day);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(12, 0));

        return slot;
    }
}
