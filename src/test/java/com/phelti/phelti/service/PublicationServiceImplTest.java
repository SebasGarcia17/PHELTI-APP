package com.phelti.phelti.service;

import com.phelti.phelti.exception.InvalidPublicationDataException;
import com.phelti.phelti.exception.PublicationNotFoundException;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.model.PublicationType;
import com.phelti.phelti.repository.PublicationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class PublicationServiceImplTest {

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private PublicationServiceImpl publicationService;

    @Test
    void createPublicationShouldCreatePublicationWhenDataIsValid() {

        Publication publication = createValidPublication();
        publication.setPublicationStatus(null);

        when(publicationRepository.createPublication(publication))
                .thenReturn(publication);

        Publication result = publicationService.createPublication(publication);

        assertNotNull(result);
        assertEquals(
                PublicationStatus.PUBLISHED.name(),
                result.getPublicationStatus()
        );

        verify(publicationRepository).createPublication(publication);
    }

    @Test
    void createPublicationShouldThrowExceptionWhenTitleIsBlank() {

        Publication publication = createValidPublication();
        publication.setTitle(" ");

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );

        verify(publicationRepository, never())
                .createPublication(any(Publication.class));
    }

    @Test
    void createPublicationShouldThrowExceptionWhenPheltAmountIsZero() {

        Publication publication = createValidPublication();
        publication.setPheltAmount(BigDecimal.ZERO);

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );

        verify(publicationRepository, never())
                .createPublication(any(Publication.class));
    }

    @Test
    void createPublicationShouldThrowExceptionWhenDurationIsNotPositive() {

        Publication publication = createValidPublication();
        publication.setDurationMinutes(0);

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );

        verify(publicationRepository, never())
                .createPublication(any(Publication.class));
    }

    @Test
    void createPublicationShouldKeepDurationWhenProvided() {

        Publication publication = createValidPublication();
        publication.setDurationMinutes(90);

        when(publicationRepository.createPublication(publication))
                .thenReturn(publication);

        Publication result = publicationService.createPublication(publication);

        assertEquals(90, result.getDurationMinutes());
    }

    @Test
    void createPublicationShouldThrowExceptionWhenUserIdIsMissing() {

        Publication publication = createValidPublication();
        publication.setUserId(null);

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );
    }

    @Test
    void createPublicationShouldThrowExceptionWhenCategoryIdIsMissing() {

        Publication publication = createValidPublication();
        publication.setCategoryId(null);

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );
    }

    @Test
    void createPublicationShouldThrowExceptionWhenPublicationTypeIsInvalid() {

        Publication publication = createValidPublication();
        publication.setPublicationType("INVALID_TYPE");

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );
    }

    @Test
    void createPublicationShouldThrowExceptionWhenInteractionModeIsInvalid() {

        Publication publication = createValidPublication();
        publication.setInteractionMode("INVALID_MODE");

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );
    }

    @Test
    void findPublicationByIdShouldReturnPublicationWhenItExists() {

        Publication publication = createValidPublication();
        publication.setPublicationId(1);

        when(publicationRepository.findPublicationById(1))
                .thenReturn(Optional.of(publication));

        Publication result = publicationService.findPublicationById(1);

        assertNotNull(result);
        assertEquals(1, result.getPublicationId());
    }

    @Test
    void findPublicationByIdShouldThrowExceptionWhenPublicationDoesNotExist() {

        when(publicationRepository.findPublicationById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                PublicationNotFoundException.class,
                () -> publicationService.findPublicationById(999)
        );
    }

    @Test
    void findAllPublicationsShouldReturnRepositoryPublications() {

        Publication firstPublication = createValidPublication();
        firstPublication.setPublicationId(1);

        Publication secondPublication = createValidPublication();
        secondPublication.setPublicationId(2);

        when(publicationRepository.findAllPublications())
                .thenReturn(List.of(
                        firstPublication,
                        secondPublication
                ));

        List<Publication> result =
                publicationService.findAllPublications();

        assertEquals(2, result.size());

        verify(publicationRepository).findAllPublications();
    }

    @Test
    void updatePublicationShouldUpdatePublicationWhenDataIsValid() {

        Integer publicationId = 1;

        Publication existing = createValidPublication();
        existing.setPublicationId(publicationId);
        existing.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        Publication updated = createValidPublication();
        updated.setTitle("Updated publication");
        updated.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        when(publicationRepository.findPublicationById(publicationId))
                .thenReturn(Optional.of(existing));

        when(publicationRepository.updatePublication(
                publicationId,
                updated
        )).thenReturn(updated);

        Publication result =
                publicationService.updatePublication(
                        publicationId,
                        updated
                );

        assertEquals(
                "Updated publication",
                result.getTitle()
        );

        verify(publicationRepository)
                .updatePublication(publicationId, updated);
    }

    @Test
    void updatePublicationShouldRejectInProgressPublication() {
        Publication existing = createValidPublication();
        existing.setPublicationStatus(PublicationStatus.IN_PROGRESS.name());
        when(publicationRepository.findPublicationById(1)).thenReturn(Optional.of(existing));
        assertThrows(InvalidPublicationDataException.class, () -> publicationService.updatePublication(1, createValidPublication()));
        verify(publicationRepository, never()).updatePublication(any(), any());
    }

    @Test
    void changePublicationStatusShouldUpdateStatus() {

        Integer publicationId = 1;

        Publication existing = createValidPublication();
        existing.setPublicationId(publicationId);
        existing.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        when(publicationRepository.findPublicationById(publicationId))
                .thenReturn(Optional.of(existing));

        when(publicationRepository.updatePublication(
                publicationId,
                existing
        )).thenReturn(existing);

        Publication result =
                publicationService.changePublicationStatus(
                        publicationId,
                        PublicationStatus.IN_PROGRESS.name()
                );

        assertEquals(
                PublicationStatus.IN_PROGRESS.name(),
                result.getPublicationStatus()
        );

        verify(publicationRepository)
                .updatePublication(
                        publicationId,
                        existing
                );
    }

    @Test
    void changePublicationStatusShouldThrowExceptionWhenStatusIsInvalid() {

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.changePublicationStatus(1, "UNKNOWN")
        );

        verify(publicationRepository, never())
                .updatePublication(any(), any());
    }

    @Test
    void changePublicationStatusShouldThrowExceptionWhenPublicationDoesNotExist() {

        when(publicationRepository.findPublicationById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                PublicationNotFoundException.class,
                () -> publicationService.changePublicationStatus(
                        999,
                        PublicationStatus.COMPLETED.name()
                )
        );
    }

    @Test
    void updatePublicationShouldThrowExceptionWhenPublicationDoesNotExist() {

        when(publicationRepository.findPublicationById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                PublicationNotFoundException.class,
                () -> publicationService.updatePublication(
                        999,
                        createValidPublication()
                )
        );

        verify(publicationRepository, never())
                .updatePublication(any(), any());
    }

    @Test
    void updatePublicationShouldThrowExceptionWhenDataIsInvalid() {

        Publication existing = createValidPublication();
        existing.setPublicationId(1);

        Publication invalid = createValidPublication();
        invalid.setTitle(" ");

        when(publicationRepository.findPublicationById(1))
                .thenReturn(Optional.of(existing));

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.updatePublication(1, invalid)
        );

        verify(publicationRepository, never())
                .updatePublication(any(), any());
    }

    @Test
    void createPublicationShouldThrowExceptionWhenStatusIsInvalid() {

        Publication publication = createValidPublication();
        publication.setPublicationStatus("UNKNOWN");

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.createPublication(publication)
        );

        verify(publicationRepository, never())
                .createPublication(any(Publication.class));
    }

    @Test
    void deletePublicationShouldWithdrawPublicationWithoutDeletingHistory() {

        Integer publicationId = 1;

        Publication publication = createValidPublication();
        publication.setPublicationId(publicationId);

        when(publicationRepository.findPublicationById(publicationId))
                .thenReturn(Optional.of(publication));

        publicationService.deletePublication(publicationId);

        verify(publicationRepository).rejectPendingProposals(publicationId);
        verify(publicationRepository).updatePublication(publicationId, publication);
        assertEquals(PublicationStatus.CANCELLED.name(), publication.getPublicationStatus());
    }

    @Test
    void deletePublicationShouldThrowExceptionWhenPublicationDoesNotExist() {

        Integer publicationId = 999;

        when(publicationRepository.findPublicationById(publicationId))
                .thenReturn(Optional.empty());

        assertThrows(
                PublicationNotFoundException.class,
                () -> publicationService.deletePublication(publicationId)
        );

        verify(publicationRepository, never())
                .deletePublication(publicationId);
    }

    @Test
    void searchPublicationsShouldDefaultToPublishedStatusAndNormalizeFilters() {

        Publication publication = createValidPublication();

        when(publicationRepository.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of(publication));

        List<Publication> result = publicationService.searchPublications(
                new PublicationFilter(
                        " offers_support ",
                        "virtual",
                        3,
                        "  guitar  ",
                        new BigDecimal("30.00"),
                        null
                )
        );

        assertEquals(List.of(publication), result);

        ArgumentCaptor<PublicationFilter> captor =
                ArgumentCaptor.forClass(PublicationFilter.class);

        verify(publicationRepository).searchPublications(captor.capture());

        PublicationFilter sent = captor.getValue();

        assertEquals(PublicationType.OFFERS_SUPPORT.name(), sent.publicationType());
        assertEquals(InteractionMode.VIRTUAL.name(), sent.interactionMode());
        assertEquals(3, sent.categoryId());
        assertEquals("guitar", sent.keyword());
        assertEquals(PublicationStatus.PUBLISHED.name(), sent.publicationStatus());
    }

    @Test
    void searchPublicationsShouldTreatBlankFiltersAsMissing() {

        when(publicationRepository.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of());

        publicationService.searchPublications(
                new PublicationFilter(" ", "", null, "   ", null, "")
        );

        ArgumentCaptor<PublicationFilter> captor =
                ArgumentCaptor.forClass(PublicationFilter.class);

        verify(publicationRepository).searchPublications(captor.capture());

        PublicationFilter sent = captor.getValue();

        assertEquals(null, sent.publicationType());
        assertEquals(null, sent.interactionMode());
        assertEquals(null, sent.keyword());
        assertEquals(PublicationStatus.PUBLISHED.name(), sent.publicationStatus());
    }

    @Test
    void searchPublicationsShouldKeepExplicitStatus() {

        when(publicationRepository.searchPublications(any(PublicationFilter.class)))
                .thenReturn(List.of());

        publicationService.searchPublications(
                new PublicationFilter(null, null, null, null, null, "completed")
        );

        ArgumentCaptor<PublicationFilter> captor =
                ArgumentCaptor.forClass(PublicationFilter.class);

        verify(publicationRepository).searchPublications(captor.capture());

        assertEquals(PublicationStatus.COMPLETED.name(), captor.getValue().publicationStatus());
    }

    @Test
    void searchPublicationsShouldThrowExceptionWhenTypeIsInvalid() {

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.searchPublications(
                        new PublicationFilter("OTHER", null, null, null, null, null)
                )
        );

        verify(publicationRepository, never()).searchPublications(any(PublicationFilter.class));
    }

    @Test
    void searchPublicationsShouldThrowExceptionWhenModeIsInvalid() {

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.searchPublications(
                        new PublicationFilter(null, "HOLOGRAM", null, null, null, null)
                )
        );
    }

    @Test
    void searchPublicationsShouldThrowExceptionWhenStatusIsInvalid() {

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.searchPublications(
                        new PublicationFilter(null, null, null, null, null, "DONE")
                )
        );
    }

    @Test
    void searchPublicationsShouldThrowExceptionWhenMaxAmountIsNotPositive() {

        assertThrows(
                InvalidPublicationDataException.class,
                () -> publicationService.searchPublications(
                        new PublicationFilter(null, null, null, null, BigDecimal.ZERO, null)
                )
        );
    }

    @Test
    void createPublicationShouldRejectMissingDuration() {
        Publication publication = createValidPublication(); publication.setDurationMinutes(null);
        assertThrows(InvalidPublicationDataException.class, () -> publicationService.createPublication(publication));
    }
    @Test
    void createPublicationShouldRejectBlankDescription() {
        Publication publication = createValidPublication(); publication.setDescription("   ");
        assertThrows(InvalidPublicationDataException.class, () -> publicationService.createPublication(publication));
    }

    private Publication createValidPublication() {

        Publication publication = new Publication();

        publication.setUserId(1);
        publication.setCategoryId(1);

        publication.setPublicationType(
                PublicationType.values()[0].name()
        );

        publication.setTitle("Test publication");
        publication.setDescription(
                "Publication used for unit testing."
        );

        publication.setInteractionMode(
                InteractionMode.values()[0].name()
        );

        publication.setPheltAmount(
                new BigDecimal("20.00")
        );

        publication.setNegotiable(true);
        publication.setDurationMinutes(60);

        publication.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        return publication;
    }
}
