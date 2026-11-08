package com.phelti.phelti.service;

import com.phelti.phelti.dto.ActivityRequest;
import com.phelti.phelti.entity.ActivityEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.exception.ActivityAlreadyExistsException;
import com.phelti.phelti.exception.ActivityNotFoundException;
import com.phelti.phelti.exception.InvalidActivityDataException;
import com.phelti.phelti.exception.InvalidActivityStateException;
import com.phelti.phelti.exception.InvalidCheckInCodeException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.model.ActivityStatus;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.model.ProposalStatus;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.model.PublicationType;
import com.phelti.phelti.repository.jpa.ActivityJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    private static final BigDecimal AMOUNT = new BigDecimal("20.00");

    @Mock
    private ActivityJpaRepository activityRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ProposalService proposalService;

    @Mock
    private PublicationService publicationService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private ActivityServiceImpl activityService;

    @Test
    void createActivityShouldMakeProposerTheRequesterWhenPublicationOffersSupport() {

        stubSchedulableProposal(PublicationType.OFFERS_SUPPORT, InteractionMode.PRESENTIAL);

        when(userRepository.findById(2))
                .thenReturn(Optional.of(createUser(2)));

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(activityRepository.save(any(ActivityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityEntity result = activityService.createActivity(createValidRequest());

        assertEquals(2, result.getRequester().getUserId());
        assertEquals(1, result.getParticipant().getUserId());
        assertEquals(10, result.getPublicationId());
        assertEquals(1, result.getProposalId());
        assertEquals(AMOUNT, result.getAgreedAmount());
        assertEquals(ActivityStatus.SCHEDULED.name(), result.getActivityStatus());
        assertEquals(InteractionMode.PRESENTIAL.name(), result.getInteractionMode());
        assertNotNull(result.getCheckInCode());

        verify(walletService).reserveBalance(2, AMOUNT);
    }

    @Test
    void createActivityShouldMakeOwnerTheRequesterWhenPublicationNeedsSupport() {

        stubSchedulableProposal(PublicationType.NEEDS_SUPPORT, InteractionMode.VIRTUAL);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(userRepository.findById(2))
                .thenReturn(Optional.of(createUser(2)));

        when(activityRepository.save(any(ActivityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityEntity result = activityService.createActivity(createValidRequest());

        assertEquals(1, result.getRequester().getUserId());
        assertEquals(2, result.getParticipant().getUserId());

        verify(walletService).reserveBalance(1, AMOUNT);
    }

    @Test
    void createActivityShouldThrowExceptionWhenProposalIdIsMissing() {

        ActivityRequest request = new ActivityRequest(
                null, futureDate(), LocalTime.of(15, 30), 60, "Library", null
        );

        assertThrows(
                InvalidActivityDataException.class,
                () -> activityService.createActivity(request)
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenDateIsInThePast() {

        ActivityRequest request = new ActivityRequest(
                1, LocalDate.now().minusDays(1), LocalTime.of(15, 30), 60, "Library", null
        );

        assertThrows(
                InvalidActivityDataException.class,
                () -> activityService.createActivity(request)
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenDurationIsNotPositive() {

        ActivityRequest request = new ActivityRequest(
                1, futureDate(), LocalTime.of(15, 30), 0, "Library", null
        );

        assertThrows(
                InvalidActivityDataException.class,
                () -> activityService.createActivity(request)
        );
    }

    @Test
    void createActivityShouldThrowExceptionWhenProposalIsNotAccepted() {

        Proposal proposal = createAcceptedProposal();
        proposal.setProposalStatus(ProposalStatus.PENDING.name());

        when(proposalService.findProposalById(1))
                .thenReturn(proposal);

        assertThrows(
                InvalidActivityStateException.class,
                () -> activityService.createActivity(createValidRequest())
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenActivityAlreadyExistsForProposal() {

        when(proposalService.findProposalById(1))
                .thenReturn(createAcceptedProposal());

        when(activityRepository.existsByProposalId(1))
                .thenReturn(true);

        assertThrows(
                ActivityAlreadyExistsException.class,
                () -> activityService.createActivity(createValidRequest())
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenPresentialActivityHasNoLocation() {

        stubSchedulableProposal(PublicationType.OFFERS_SUPPORT, InteractionMode.PRESENTIAL);

        ActivityRequest request = new ActivityRequest(
                1, futureDate(), LocalTime.of(15, 30), 60, " ", null
        );

        assertThrows(
                InvalidActivityDataException.class,
                () -> activityService.createActivity(request)
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenVirtualActivityHasNoLink() {

        stubSchedulableProposal(PublicationType.OFFERS_SUPPORT, InteractionMode.VIRTUAL);

        ActivityRequest request = new ActivityRequest(
                1, futureDate(), LocalTime.of(15, 30), 60, null, null
        );

        assertThrows(
                InvalidActivityDataException.class,
                () -> activityService.createActivity(request)
        );

        verify(walletService, never()).reserveBalance(any(), any());
    }

    @Test
    void createActivityShouldThrowExceptionWhenUserDoesNotExist() {

        stubSchedulableProposal(PublicationType.OFFERS_SUPPORT, InteractionMode.PRESENTIAL);

        when(userRepository.findById(2))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> activityService.createActivity(createValidRequest())
        );

        verify(walletService, never()).reserveBalance(any(), any());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void findActivityByIdShouldReturnActivityWhenItExists() {

        ActivityEntity activity = createScheduledActivity();

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        assertEquals(activity, activityService.findActivityById(1));
    }

    @Test
    void findActivityByIdShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ActivityNotFoundException.class,
                () -> activityService.findActivityById(999)
        );
    }

    @Test
    void findActivitiesByUserIdShouldReturnActivitiesWhenUserExists() {

        List<ActivityEntity> activities = List.of(createScheduledActivity());

        when(userRepository.findById(1))
                .thenReturn(Optional.of(createUser(1)));

        when(activityRepository.findByParticipantOrRequesterId(1))
                .thenReturn(activities);

        assertEquals(activities, activityService.findActivitiesByUserId(1));
    }

    @Test
    void findActivitiesByUserIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> activityService.findActivitiesByUserId(999)
        );

        verify(activityRepository, never()).findByParticipantOrRequesterId(any());
    }

    @Test
    void completeActivityShouldTransferPhelstAndCompleteWhenCodeMatches() {

        ActivityEntity activity = createScheduledActivity();

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        when(activityRepository.save(activity))
                .thenReturn(activity);

        ActivityEntity result = activityService.completeActivity(1, "CODE-123");

        assertEquals(ActivityStatus.COMPLETED.name(), result.getActivityStatus());

        verify(walletService).transferReservedBalance(2, 1, AMOUNT);
        verify(publicationService)
                .changePublicationStatus(10, PublicationStatus.COMPLETED.name());
    }

    @Test
    void completeActivityShouldThrowExceptionWhenCodeDoesNotMatch() {

        ActivityEntity activity = createScheduledActivity();

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        assertThrows(
                InvalidCheckInCodeException.class,
                () -> activityService.completeActivity(1, "WRONG")
        );

        verify(walletService, never()).transferReservedBalance(any(), any(), any());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void completeActivityShouldThrowExceptionWhenActivityIsNotScheduled() {

        ActivityEntity activity = createScheduledActivity();
        activity.setActivityStatus(ActivityStatus.CANCELLED.name());

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        assertThrows(
                InvalidActivityStateException.class,
                () -> activityService.completeActivity(1, "CODE-123")
        );

        verify(walletService, never()).transferReservedBalance(any(), any(), any());
    }

    @Test
    void completeActivityShouldThrowExceptionWhenActivityDoesNotExist() {

        when(activityRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ActivityNotFoundException.class,
                () -> activityService.completeActivity(999, "CODE-123")
        );
    }

    @Test
    void cancelActivityShouldReleaseReservedPhelstAndRepublishPublication() {

        ActivityEntity activity = createScheduledActivity();

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        when(activityRepository.save(activity))
                .thenReturn(activity);

        ActivityEntity result = activityService.cancelActivity(1);

        assertEquals(ActivityStatus.CANCELLED.name(), result.getActivityStatus());

        verify(walletService).releaseReservedBalance(2, AMOUNT);
        verify(proposalService).cancelAcceptedProposal(activity.getProposalId());
        verify(publicationService)
                .changePublicationStatus(10, PublicationStatus.PUBLISHED.name());
    }

    @Test
    void cancelActivityShouldThrowExceptionWhenActivityIsNotScheduled() {

        ActivityEntity activity = createScheduledActivity();
        activity.setActivityStatus(ActivityStatus.COMPLETED.name());

        when(activityRepository.findById(1))
                .thenReturn(Optional.of(activity));

        assertThrows(
                InvalidActivityStateException.class,
                () -> activityService.cancelActivity(1)
        );

        verify(walletService, never()).releaseReservedBalance(any(), any());
        verify(proposalService, never()).cancelAcceptedProposal(any());
    }

    private void stubSchedulableProposal(PublicationType type, InteractionMode mode) {

        when(proposalService.findProposalById(1))
                .thenReturn(createAcceptedProposal());

        when(activityRepository.existsByProposalId(1))
                .thenReturn(false);

        when(publicationService.findPublicationById(10))
                .thenReturn(createPublication(type, mode));
    }

    private Proposal createAcceptedProposal() {

        Proposal proposal = new Proposal();
        proposal.setProposalId(1);
        proposal.setPublicationId(10);
        proposal.setProposingUserId(2);
        proposal.setProposalStatus(ProposalStatus.ACCEPTED.name());

        return proposal;
    }

    private Publication createPublication(PublicationType type, InteractionMode mode) {

        Publication publication = new Publication();
        publication.setPublicationId(10);
        publication.setUserId(1);
        publication.setPublicationType(type.name());
        publication.setInteractionMode(mode.name());
        publication.setPheltAmount(AMOUNT);
        publication.setPublicationStatus(PublicationStatus.IN_PROGRESS.name());

        return publication;
    }

    private ActivityRequest createValidRequest() {

        return new ActivityRequest(
                1,
                futureDate(),
                LocalTime.of(15, 30),
                60,
                "Central Library",
                "https://meet.example.com/abc"
        );
    }

    private ActivityEntity createScheduledActivity() {

        ActivityEntity activity = new ActivityEntity();
        activity.setActivityId(1);
        activity.setPublicationId(10);
        activity.setProposalId(1);
        activity.setRequester(createUser(2));
        activity.setParticipant(createUser(1));
        activity.setAgreedAmount(AMOUNT);
        activity.setCheckInCode("CODE-123");
        activity.setActivityStatus(ActivityStatus.SCHEDULED.name());

        return activity;
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        return user;
    }

    private LocalDate futureDate() {

        return LocalDate.now().plusDays(3);
    }
}
