package com.phelti.phelti.controller;

import com.phelti.phelti.model.Proposal;
import com.phelti.phelti.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "Create a proposal for a publication")
    @PostMapping
    public ResponseEntity<Proposal> createProposal(@RequestBody Proposal proposal) {
        Proposal created = proposalService.createProposal(proposal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all proposals")
    @GetMapping
    public ResponseEntity<List<Proposal>> findAllProposals() {
        return ResponseEntity.ok(proposalService.findAllProposals());
    }

    @Operation(summary = "Get a proposal by id")
    @GetMapping("/{id}")
    public ResponseEntity<Proposal> findProposalById(@PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(proposalService.findProposalById(proposalId));
    }

    @Operation(summary = "Accept a proposal")
    @PutMapping("/{id}/accept")
    public ResponseEntity<Proposal> acceptProposal(@PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(proposalService.acceptProposal(proposalId));
    }

    @Operation(summary = "Reject a proposal")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Proposal> rejectProposal(@PathVariable("id") Integer proposalId) {
        return ResponseEntity.ok(proposalService.rejectProposal(proposalId));
    }

    @Operation(summary = "Delete a proposal")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProposal(@PathVariable("id") Integer proposalId) {
        proposalService.deleteProposal(proposalId);
        return ResponseEntity.noContent().build();
    }
}
