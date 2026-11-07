package com.phelti.phelti.service;

import com.phelti.phelti.exception.InvalidProposalDataException;
import com.phelti.phelti.exception.ProposalNotFoundException;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.model.ProposalStatus;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.repository.ProposalRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalServiceImplTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private PublicationService publicationService;

    @InjectMocks
    private ProposalServiceImpl proposalService;

    @Test
    void createProposalShouldCreatePendingProposalWhenDataIsValid() {

        Proposal proposal = createValidProposal();

        Publication publication = new Publication();
        publication.setPublicationStatus(PublicationStatus.PUBLISHED.name());

        when(publicationService.findPublicationById(1))
                .thenReturn(publication);

        when(proposalRepository.createProposal(proposal))
                .thenReturn(proposal);

        Proposal result = proposalService.createProposal(proposal);

        assertNotNull(result);
        assertEquals(
                ProposalStatus.PENDING.name(),
                result.getProposalStatus()
        );

        verify(proposalRepository).createProposal(proposal);
    }

    @Test
    void createProposalShouldThrowExceptionWhenPublicationIsNotPublished() {

        Proposal proposal = createValidProposal();

        Publication publication = new Publication();
        publication.setPublicationStatus(
                PublicationStatus.IN_PROGRESS.name()
        );

        when(publicationService.findPublicationById(1))
                .thenReturn(publication);

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.createProposal(proposal)
        );

        verify(proposalRepository, never())
                .createProposal(proposal);
    }

    @Test
    void createProposalShouldThrowExceptionWhenPublicationIdIsMissing() {

        Proposal proposal = createValidProposal();
        proposal.setPublicationId(null);

        Publication publication = new Publication();
        publication.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        when(publicationService.findPublicationById(null))
                .thenReturn(publication);

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.createProposal(proposal)
        );
    }

    @Test
    void createProposalShouldThrowExceptionWhenProposingUserIdIsMissing() {

        Proposal proposal = createValidProposal();
        proposal.setProposingUserId(null);

        Publication publication = new Publication();
        publication.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        when(publicationService.findPublicationById(1))
                .thenReturn(publication);

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.createProposal(proposal)
        );
    }

    @Test
    void createProposalShouldThrowExceptionWhenMessageIsTooLong() {

        Proposal proposal = createValidProposal();
        proposal.setMessage("a".repeat(501));

        Publication publication = new Publication();
        publication.setPublicationStatus(
                PublicationStatus.PUBLISHED.name()
        );

        when(publicationService.findPublicationById(1))
                .thenReturn(publication);

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.createProposal(proposal)
        );
    }

    @Test
    void findProposalByIdShouldReturnProposalWhenItExists() {

        Proposal proposal = createValidProposal();
        proposal.setProposalId(1);

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(proposal));

        Proposal result = proposalService.findProposalById(1);

        assertNotNull(result);
        assertEquals(1, result.getProposalId());
    }

    @Test
    void findProposalByIdShouldThrowExceptionWhenProposalDoesNotExist() {

        when(proposalRepository.findProposalById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ProposalNotFoundException.class,
                () -> proposalService.findProposalById(999)
        );
    }

    @Test
    void findAllProposalsShouldReturnRepositoryProposals() {

        Proposal proposalOne = createValidProposal();
        proposalOne.setProposalId(1);

        Proposal proposalTwo = createValidProposal();
        proposalTwo.setProposalId(2);

        when(proposalRepository.findAllProposals())
                .thenReturn(List.of(
                        proposalOne,
                        proposalTwo
                ));

        List<Proposal> result =
                proposalService.findAllProposals();

        assertEquals(2, result.size());

        verify(proposalRepository)
                .findAllProposals();
    }

    @Test
    void acceptProposalShouldAcceptPendingProposal() {

        Proposal acceptedProposal = createValidProposal();
        acceptedProposal.setProposalId(1);
        acceptedProposal.setProposalStatus(
                ProposalStatus.PENDING.name()
        );

        Proposal otherProposal = createValidProposal();
        otherProposal.setProposalId(2);
        otherProposal.setProposalStatus(
                ProposalStatus.PENDING.name()
        );

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(acceptedProposal));

        when(proposalRepository.findProposalsByPublicationId(1))
                .thenReturn(List.of(
                        acceptedProposal,
                        otherProposal
                ));

        Proposal result =
                proposalService.acceptProposal(1);

        assertEquals(
                ProposalStatus.ACCEPTED.name(),
                result.getProposalStatus()
        );

        verify(proposalRepository)
                .updateProposalStatus(
                        1,
                        ProposalStatus.ACCEPTED.name()
                );

        verify(publicationService)
                .changePublicationStatus(
                        1,
                        PublicationStatus.IN_PROGRESS.name()
                );

        verify(proposalRepository)
                .updateProposalStatus(
                        2,
                        ProposalStatus.REJECTED.name()
                );
    }

    @Test
    void acceptProposalShouldThrowExceptionWhenProposalIsNotPending() {

        Proposal proposal = createValidProposal();
        proposal.setProposalId(1);
        proposal.setProposalStatus(
                ProposalStatus.REJECTED.name()
        );

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(proposal));

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.acceptProposal(1)
        );
    }

    @Test
    void rejectProposalShouldRejectPendingProposal() {

        Proposal proposal = createValidProposal();
        proposal.setProposalId(1);
        proposal.setProposalStatus(
                ProposalStatus.PENDING.name()
        );

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(proposal));

        Proposal result =
                proposalService.rejectProposal(1);

        assertEquals(
                ProposalStatus.REJECTED.name(),
                result.getProposalStatus()
        );

        verify(proposalRepository)
                .updateProposalStatus(
                        1,
                        ProposalStatus.REJECTED.name()
                );
    }

    @Test
    void rejectProposalShouldThrowExceptionWhenProposalIsNotPending() {

        Proposal proposal = createValidProposal();
        proposal.setProposalId(1);
        proposal.setProposalStatus(
                ProposalStatus.ACCEPTED.name()
        );

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(proposal));

        assertThrows(
                InvalidProposalDataException.class,
                () -> proposalService.rejectProposal(1)
        );
    }

    @Test
    void deleteProposalShouldDeleteProposalWhenItExists() {

        Proposal proposal = createValidProposal();
        proposal.setProposalId(1);

        when(proposalRepository.findProposalById(1))
                .thenReturn(Optional.of(proposal));

        proposalService.deleteProposal(1);

        verify(proposalRepository)
                .deleteProposal(1);
    }

    @Test
    void deleteProposalShouldThrowExceptionWhenProposalDoesNotExist() {

        when(proposalRepository.findProposalById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ProposalNotFoundException.class,
                () -> proposalService.deleteProposal(999)
        );

        verify(proposalRepository, never())
                .deleteProposal(999);
    }

    private Proposal createValidProposal() {

        Proposal proposal = new Proposal();

        proposal.setPublicationId(1);
        proposal.setProposingUserId(2);
        proposal.setMessage(
                "I can help with this activity."
        );

        return proposal;
    }
}