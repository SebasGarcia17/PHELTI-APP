package com.phelti.phelti.service;

import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.InterestAlreadyAssignedException;
import com.phelti.phelti.exception.InterestNotAssignedException;
import com.phelti.phelti.exception.InterestNotFoundException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.InterestJpaRepository;
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
class InterestServiceImplTest {

    @Mock
    private InterestJpaRepository interestRepository;

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private InterestServiceImpl interestService;

    @Test
    void findAllInterestsShouldReturnRepositoryInterests() {

        List<InterestEntity> items = List.of(new InterestEntity(), new InterestEntity());

        when(interestRepository.findAllByOrderByNameAsc())
                .thenReturn(items);

        assertEquals(items, interestService.findAllInterests());
    }

    @Test
    void findInterestsByUserIdShouldReturnTheItemsOfTheUser() {

        UserEntity user = createUser(1);
        InterestEntity item = createInterest(1, "Guitar");
        user.getInterests().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        assertEquals(List.of(item), interestService.findInterestsByUserId(1));
    }

    @Test
    void findInterestsByUserIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> interestService.findInterestsByUserId(9)
        );
    }

    @Test
    void addInterestToUserShouldAssignTheItemWhenItIsNotAssigned() {

        UserEntity user = createUser(1);
        InterestEntity item = createInterest(5, "Guitar");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(interestRepository.findById(5))
                .thenReturn(Optional.of(item));

        interestService.addInterestToUser(1, 5);

        assertTrue(user.getInterests().contains(item));
        verify(userRepository).save(user);
    }

    @Test
    void addInterestToUserShouldThrowExceptionWhenItIsAlreadyAssigned() {

        UserEntity user = createUser(1);
        InterestEntity item = createInterest(5, "Guitar");
        user.getInterests().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(interestRepository.findById(5))
                .thenReturn(Optional.of(item));

        assertThrows(
                InterestAlreadyAssignedException.class,
                () -> interestService.addInterestToUser(1, 5)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void addInterestToUserShouldThrowExceptionWhenInterestDoesNotExist() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(interestRepository.findById(5))
                .thenReturn(Optional.empty());

        assertThrows(
                InterestNotFoundException.class,
                () -> interestService.addInterestToUser(1, 5)
        );
    }

    @Test
    void addInterestToUserShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> interestService.addInterestToUser(9, 5)
        );

        verify(interestRepository, never()).findById(any());
    }

    @Test
    void removeInterestFromUserShouldRemoveTheItemWhenItIsAssigned() {

        UserEntity user = createUser(1);
        InterestEntity item = createInterest(5, "Guitar");
        user.getInterests().add(item);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(interestRepository.findById(5))
                .thenReturn(Optional.of(item));

        interestService.removeInterestFromUser(1, 5);

        assertFalse(user.getInterests().contains(item));
        verify(userRepository).save(user);
    }

    @Test
    void removeInterestFromUserShouldThrowExceptionWhenItIsNotAssigned() {

        UserEntity user = createUser(1);
        InterestEntity item = createInterest(5, "Guitar");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(interestRepository.findById(5))
                .thenReturn(Optional.of(item));

        assertThrows(
                InterestNotAssignedException.class,
                () -> interestService.removeInterestFromUser(1, 5)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        return user;
    }

    private InterestEntity createInterest(Integer id, String name) {

        InterestEntity item = new InterestEntity();
        item.setInterestId(id);
        item.setName(name);

        return item;
    }
}
