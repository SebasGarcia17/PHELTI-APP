package com.phelti.phelti.controller;

import com.phelti.phelti.dto.ProposalRequest;
import com.phelti.phelti.dto.ProposalResponse;
import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
@Tag(name = "Proposals", description = "Operations for proposals made on publications")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @Operation(
            summary = "Create a proposal for a publication",
            description = "The publication must be PUBLISHED and the proposing user cannot be its owner. The proposal starts as PENDING"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proposal created"),
            @ApiResponse(responseCode = "400", description = "Invalid proposal data or publication not available"),
            @ApiResponse(responseCode = "404", description = "Publication not found"),
            @ApiResponse(responseCode = "409", description = "The proposing user does not exist")
    })
    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(
            @Valid @RequestBody ProposalRequest request) {
        Proposal created = proposalService.createProposal(toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(summary = "List all proposals")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposals listed")
    })
    @GetMapping
    public ResponseEntity<List<ProposalResponse>> findAllProposals() {
        List<ProposalResponse> proposals = proposalService.findAllProposals()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(proposals);
    }

    @Operation(summary = "Get a proposal by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposal found"),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProposalResponse> findProposalById(
            @Parameter(description = "Proposal identifier", example = "1")
            @PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(toResponse(proposalService.findProposalById(proposalId)));
    }

    @Operation(
            summary = "Accept a proposal",
            description = "Moves the publication to IN_PROGRESS and rejects the other pending proposals of that publication"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposal accepted"),
            @ApiResponse(responseCode = "400", description = "Only pending proposals can be accepted"),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PutMapping("/{id}/accept")
    public ResponseEntity<ProposalResponse> acceptProposal(
            @Parameter(description = "Proposal identifier", example = "1")
            @PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(toResponse(proposalService.acceptProposal(proposalId)));
    }

    @Operation(summary = "Reject a proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposal rejected"),
            @ApiResponse(responseCode = "400", description = "Only pending proposals can be rejected"),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<ProposalResponse> rejectProposal(
            @Parameter(description = "Proposal identifier", example = "1")
            @PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(toResponse(proposalService.rejectProposal(proposalId)));
    }

    @Operation(
            summary = "Delete a proposal",
            description = "Fails when an activity already references the proposal"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proposal deleted"),
            @ApiResponse(responseCode = "404", description = "Proposal not found"),
            @ApiResponse(responseCode = "409", description = "The proposal is referenced by an activity")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProposal(
            @Parameter(description = "Proposal identifier", example = "1")
            @PathVariable("id") Integer proposalId) {
        proposalService.deleteProposal(proposalId);
        return ResponseEntity.noContent().build();
    }

    private Proposal toModel(ProposalRequest request) {
        Proposal proposal = new Proposal();
        proposal.setPublicationId(request.publicationId());
        proposal.setProposingUserId(request.proposingUserId());
        proposal.setMessage(request.message());
        return proposal;
    }

    private ProposalResponse toResponse(Proposal proposal) {
        return new ProposalResponse(
                proposal.getProposalId(),
                proposal.getPublicationId(),
                proposal.getProposingUserId(),
                proposal.getMessage(),
                proposal.getProposalStatus(),
                proposal.getCreatedAt()
        );
    }
}
