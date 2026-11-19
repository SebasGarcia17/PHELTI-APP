package com.phelti.phelti.service;

import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.ConfirmationEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.ActivityNotFoundException;
import com.phelti.phelti.exception.ConfirmationAlreadyExistsException;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidCheckInCodeException;
import com.phelti.phelti.exception.InvalidConfirmationException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.repository.jpa.ConfirmationJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationServiceImplTest {

    private static final String CODE = "ABC123";

    @Mock
    private ConfirmationJpaRepository confirmationRepository;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ConfirmationServiceImpl confirmationService;

    @Test
    void confirmActivityShouldRegisterConfirmationAndKeepActivityScheduledWhenOnlyOneUserConfirmed() {

        ActivityEntity activity = createActivity(ActivityStatus.SCHEDULED);

        when(activityService.findActivityById(1))
                .thenReturn(activity);

        when(confirmationRepository.existsByActivityActivityIdAndUserUserId(1, 2))
                .thenReturn(false);

        when(confirmationRepository.countByActivityActivityId(1))
                .thenReturn(1L);

        ActivityEntity result = confirmationService.confirmActivity(1, 2, CODE);

        assertSame(activity, result);

        ArgumentCaptor<ConfirmationEntity> captor =
                ArgumentCaptor.forClass(ConfirmationEntity.class);

        verify(confirmationRepository).save(captor.capture());

        assertEquals(activity, captor.getValue().getActivity());
        assertEquals(activity.getRequester(), captor.getValue().getUser());
        assertNotNull(captor.getValue().getConfirmedAt());

        verify(activityService, never()).completeActivity(any(), any());
    }

    @Test
    void confirmActivityShouldCompleteTheActivityWhenBothUsersConfirmed() {

        ActivityEntity activity = createActivity(ActivityStatus.SCHEDULED);
        ActivityEntity completed = createActivity(ActivityStatus.COMPLETED);

        when(activityService.findActivityById(1))
                .thenReturn(activity);

        when(confirmationRepository.existsByActivityActivityIdAndUserUserId(1, 1))
                .thenReturn(false);

        when(confirmationRepository.countByActivityActivityId(1))
                .thenReturn(2L);

        when(activityService.completeActivity(1, CODE))
                .thenReturn(completed);

        ActivityEntity result = confirmationService.confirmActivity(1, 1, CODE);

        assertSame(completed, result);

        ArgumentCaptor<ConfirmationEntity> captor =
                ArgumentCaptor.forClass(ConfirmationEntity.class);

        verify(confirmationRepository).save(captor.capture());

        assertEquals(activity.getParticipant(), captor.getValue().getUser());
        verify(activityService).completeActivity(1, CODE);
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenUserIdIsMissing() {

        assertThrows(
                InvalidConfirmationException.class,
                () -> confirmationService.confirmActivity(1, null, CODE)
        );

        verify(confirmationRepository, never()).save(any(ConfirmationEntity.class));
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityService.findActivityById(9))
                .thenThrow(new ActivityNotFoundException(9));

        assertThrows(
                ActivityNotFoundException.class,
                () -> confirmationService.confirmActivity(9, 1, CODE)
        );
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenActivityIsNotScheduled() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.COMPLETED));

        assertThrows(
                InvalidActivityStateException.class,
                () -> confirmationService.confirmActivity(1, 1, CODE)
        );

        verify(confirmationRepository, never()).save(any(ConfirmationEntity.class));
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenUserIsNotPartOfTheActivity() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        assertThrows(
                InvalidConfirmationException.class,
                () -> confirmationService.confirmActivity(1, 99, CODE)
        );

        verify(confirmationRepository, never()).save(any(ConfirmationEntity.class));
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenCheckInCodeDoesNotMatch() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        assertThrows(
                InvalidCheckInCodeException.class,
                () -> confirmationService.confirmActivity(1, 1, "WRONG")
        );

        verify(confirmationRepository, never()).save(any(ConfirmationEntity.class));
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenCheckInCodeIsMissing() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        assertThrows(
                InvalidCheckInCodeException.class,
                () -> confirmationService.confirmActivity(1, 1, null)
        );
    }

    @Test
    void confirmActivityShouldThrowExceptionWhenUserAlreadyConfirmed() {

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        when(confirmationRepository.existsByActivityActivityIdAndUserUserId(1, 1))
                .thenReturn(true);

        assertThrows(
                ConfirmationAlreadyExistsException.class,
                () -> confirmationService.confirmActivity(1, 1, CODE)
        );

        verify(confirmationRepository, never()).save(any(ConfirmationEntity.class));
    }

    @Test
    void findConfirmationsByActivityIdShouldReturnRepositoryConfirmations() {

        List<ConfirmationEntity> confirmations = List.of(new ConfirmationEntity());

        when(activityService.findActivityById(1))
                .thenReturn(createActivity(ActivityStatus.SCHEDULED));

        when(confirmationRepository.findByActivityId(1))
                .thenReturn(confirmations);

        assertEquals(confirmations, confirmationService.findConfirmationsByActivityId(1));
    }

    @Test
    void findConfirmationsByActivityIdShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityService.findActivityById(9))
                .thenThrow(new ActivityNotFoundException(9));

        assertThrows(
                ActivityNotFoundException.class,
                () -> confirmationService.findConfirmationsByActivityId(9)
        );

        verify(confirmationRepository, never()).findByActivityId(any());
    }

    private ActivityEntity createActivity(ActivityStatus status) {

        UserEntity requester = new UserEntity();
        requester.setUserId(2);

        UserEntity participant = new UserEntity();
        participant.setUserId(1);

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityId(1);
        activity.setRequester(requester);
        activity.setParticipant(participant);
        activity.setCheckInCode(CODE);
        activity.setActivityStatus(status.name());

        return activity;
    }
}
