package com.phelti.phelti.service;

import com.phelti.phelti.entity.AvailabilityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.AvailabilityNotFoundException;
import com.phelti.phelti.exception.AvailabilityOverlapException;
import com.phelti.phelti.exception.InvalidAvailabilityDataException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.repository.jpa.AvailabilityJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityJpaRepository availabilityRepository;

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Test
    void addAvailabilityShouldSaveSlotWhenDataIsValid() {

        UserEntity user = createUser(1);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(availabilityRepository.findByUserUserIdAndDayOfWeek(1, "SATURDAY"))
                .thenReturn(List.of());

        when(availabilityRepository.save(any(AvailabilityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityEntity result = availabilityService.addAvailability(
                1, "saturday", LocalTime.of(9, 0), LocalTime.of(12, 0)
        );

        assertEquals(user, result.getUser());
        assertEquals("SATURDAY", result.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getEndTime());
    }

    @Test
    void addAvailabilityShouldAllowAdjacentSlots() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(availabilityRepository.findByUserUserIdAndDayOfWeek(1, "MONDAY"))
                .thenReturn(List.of(slot("MONDAY", 9, 12)));

        when(availabilityRepository.save(any(AvailabilityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityEntity result = availabilityService.addAvailability(
                1, "MONDAY", LocalTime.of(12, 0), LocalTime.of(14, 0)
        );

        assertEquals(LocalTime.of(12, 0), result.getStartTime());
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenSlotOverlapsAnExistingOne() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(availabilityRepository.findByUserUserIdAndDayOfWeek(1, "MONDAY"))
                .thenReturn(List.of(slot("MONDAY", 9, 12)));

        assertThrows(
                AvailabilityOverlapException.class,
                () -> availabilityService.addAvailability(
                        1, "MONDAY", LocalTime.of(11, 0), LocalTime.of(13, 0)
                )
        );

        verify(availabilityRepository, never()).save(any(AvailabilityEntity.class));
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenDayIsInvalid() {

        assertThrows(
                InvalidAvailabilityDataException.class,
                () -> availabilityService.addAvailability(
                        1, "FUNDAY", LocalTime.of(9, 0), LocalTime.of(12, 0)
                )
        );
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenDayIsMissing() {

        assertThrows(
                InvalidAvailabilityDataException.class,
                () -> availabilityService.addAvailability(
                        1, " ", LocalTime.of(9, 0), LocalTime.of(12, 0)
                )
        );
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenEndIsNotAfterStart() {

        assertThrows(
                InvalidAvailabilityDataException.class,
                () -> availabilityService.addAvailability(
                        1, "MONDAY", LocalTime.of(12, 0), LocalTime.of(12, 0)
                )
        );
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenHoursAreMissing() {

        assertThrows(
                InvalidAvailabilityDataException.class,
                () -> availabilityService.addAvailability(1, "MONDAY", null, LocalTime.of(12, 0))
        );
    }

    @Test
    void addAvailabilityShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(9))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> availabilityService.addAvailability(
                        9, "MONDAY", LocalTime.of(9, 0), LocalTime.of(12, 0)
                )
        );

        verify(availabilityRepository, never()).save(any(AvailabilityEntity.class));
    }

    @Test
    void findAvailabilityByUserIdShouldReturnSlotsSortedByDayAndStart() {

        AvailabilityEntity fridayMorning = slot("FRIDAY", 8, 10);
        AvailabilityEntity mondayLate = slot("MONDAY", 15, 17);
        AvailabilityEntity mondayEarly = slot("MONDAY", 8, 10);

        when(userRepository.existsById(1))
                .thenReturn(true);

        when(availabilityRepository.findByUserUserId(1))
                .thenReturn(List.of(fridayMorning, mondayLate, mondayEarly));

        List<AvailabilityEntity> result = availabilityService.findAvailabilityByUserId(1);

        assertEquals(List.of(mondayEarly, mondayLate, fridayMorning), result);
    }

    @Test
    void findAvailabilityByUserIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.existsById(9))
                .thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> availabilityService.findAvailabilityByUserId(9)
        );
    }

    @Test
    void deleteAvailabilityShouldDeleteTheSlotWhenItExists() {

        AvailabilityEntity slot = slot("MONDAY", 9, 12);

        when(availabilityRepository.findById(3))
                .thenReturn(Optional.of(slot));

        availabilityService.deleteAvailability(3);

        verify(availabilityRepository).delete(slot);
    }

    @Test
    void deleteAvailabilityShouldThrowExceptionWhenSlotDoesNotExist() {

        when(availabilityRepository.findById(3))
                .thenReturn(Optional.empty());

        assertThrows(
                AvailabilityNotFoundException.class,
                () -> availabilityService.deleteAvailability(3)
        );

        verify(availabilityRepository, never()).delete(any(AvailabilityEntity.class));
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        return user;
    }

    private AvailabilityEntity slot(String day, int startHour, int endHour) {

        AvailabilityEntity slot = new AvailabilityEntity();
        slot.setDayOfWeek(day);
        slot.setStartTime(LocalTime.of(startHour, 0));
        slot.setEndTime(LocalTime.of(endHour, 0));

        return slot;
    }
}
