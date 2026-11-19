package com.phelti.phelti.service;

import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.InvalidSkillDataException;
import com.phelti.phelti.exception.SkillAlreadyAssignedException;
import com.phelti.phelti.exception.SkillAlreadyExistsException;
import com.phelti.phelti.exception.SkillNotAssignedException;
import com.phelti.phelti.exception.SkillNotFoundException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.SkillJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillJpaRepository skillRepository;

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void createSkillShouldSaveTrimmedNameWhenDataIsValid() {

        when(skillRepository.existsByNameIgnoreCase("Guitar"))
                .thenReturn(false);

        when(skillRepository.save(any(SkillEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SkillEntity result = skillService.createSkill("  Guitar ");

        assertEquals("Guitar", result.getName());
        verify(skillRepository).save(result);
    }

    @Test
    void createSkillShouldThrowExceptionWhenNameIsBlank() {

        assertThrows(
                InvalidSkillDataException.class,
                () -> skillService.createSkill(" ")
        );

        verify(skillRepository, never()).save(any(SkillEntity.class));
    }

    @Test
    void createSkillShouldThrowExceptionWhenNameIsTooLong() {

        String longName = "x".repeat(81);

        assertThrows(
                InvalidSkillDataException.class,
                () -> skillService.createSkill(longName)
        );
    }

    @Test
    void createSkillShouldThrowExceptionWhenSkillAlreadyExists() {

        when(skillRepository.existsByNameIgnoreCase("Guitar"))
                .thenReturn(true);

        assertThrows(
                SkillAlreadyExistsException.class,
                () -> skillService.createSkill("Guitar")
        );

        verify(skillRepository, never()).save(any(SkillEntity.class));
    }

    @Test
    void findAllSkillsShouldReturnRepositorySkills() {

        List<SkillEntity> items = List.of(new SkillEntity(), new SkillEntity());

        when(skillRepository.findAllByOrderByNameAsc())
                .thenReturn(items);

        assertEquals(items, skillService.findAllSkills());
    }

    @Test
    void findSkillsByUserIdShouldReturnTheItemsOfTheUser() {

        UserEntity user = createUser(1);
        SkillEntity item = createSkill(1, "Guitar");
        user.getSkills().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        assertEquals(List.of(item), skillService.findSkillsByUserId(1));
    }

    @Test
    void findSkillsByUserIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> skillService.findSkillsByUserId(9)
        );
    }

    @Test
    void addSkillToUserShouldAssignTheItemWhenItIsNotAssigned() {

        UserEntity user = createUser(1);
        SkillEntity item = createSkill(5, "Guitar");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(skillRepository.findById(5))
                .thenReturn(Optional.of(item));

        skillService.addSkillToUser(1, 5);

        assertTrue(user.getSkills().contains(item));
        verify(userRepository).save(user);
    }

    @Test
    void addSkillToUserShouldThrowExceptionWhenItIsAlreadyAssigned() {

        UserEntity user = createUser(1);
        SkillEntity item = createSkill(5, "Guitar");
        user.getSkills().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(skillRepository.findById(5))
                .thenReturn(Optional.of(item));

        assertThrows(
                SkillAlreadyAssignedException.class,
                () -> skillService.addSkillToUser(1, 5)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void addSkillToUserShouldThrowExceptionWhenSkillDoesNotExist() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(skillRepository.findById(5))
                .thenReturn(Optional.empty());

        assertThrows(
                SkillNotFoundException.class,
                () -> skillService.addSkillToUser(1, 5)
        );
    }

    @Test
    void addSkillToUserShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> skillService.addSkillToUser(9, 5)
        );

        verify(skillRepository, never()).findById(any());
    }

    @Test
    void removeSkillFromUserShouldRemoveTheItemWhenItIsAssigned() {

        UserEntity user = createUser(1);
        SkillEntity item = createSkill(5, "Guitar");
        user.getSkills().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(skillRepository.findById(5))
                .thenReturn(Optional.of(item));

        skillService.removeSkillFromUser(1, 5);

        assertFalse(user.getSkills().contains(item));
        verify(userRepository).save(user);
    }

    @Test
    void removeSkillFromUserShouldThrowExceptionWhenItIsNotAssigned() {

        UserEntity user = createUser(1);
        SkillEntity item = createSkill(5, "Guitar");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(skillRepository.findById(5))
                .thenReturn(Optional.of(item));

        assertThrows(
                SkillNotAssignedException.class,
                () -> skillService.removeSkillFromUser(1, 5)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        return user;
    }

    private SkillEntity createSkill(Integer id, String name) {

        SkillEntity item = new SkillEntity();
        item.setSkillId(id);
        item.setName(name);

        return item;
    }
}
