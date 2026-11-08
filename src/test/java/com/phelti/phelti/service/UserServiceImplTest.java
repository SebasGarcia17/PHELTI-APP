package com.phelti.phelti.service;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
