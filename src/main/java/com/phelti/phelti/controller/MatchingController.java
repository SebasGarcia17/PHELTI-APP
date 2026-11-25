package com.phelti.phelti.controller;

import com.phelti.phelti.dto.PublicationMatchResponse;
import com.phelti.phelti.dto.PublicationResponse;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationMatch;
import com.phelti.phelti.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@Tag(name = "Matching", description = "Pairing of users with compatible publications")
public class MatchingController {

    private final com.phelti.phelti.config.DemoAccess access;

    private final MatchingService matchingService;

    public MatchingController(com.phelti.phelti.config.DemoAccess access, MatchingService matchingService) {
        this.access = access;
        this.matchingService = matchingService;
    }

    @Operation(
            summary = "Find the publications compatible with a user",
            description = "Ranks the PUBLISHED publications of other users by compatibility: skills for requests of support, interests for offers of support, preferred interaction mode and shared availability"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compatible publications listed, best first"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/publications")
    public ResponseEntity<List<PublicationMatchResponse>> findMatches(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {
        access.own(userId);

        List<PublicationMatchResponse> matches = matchingService.findMatchesForUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(matches);
    }

    private PublicationMatchResponse toResponse(PublicationMatch match) {

        return new PublicationMatchResponse(
                toPublicationResponse(match.publication()),
                match.score(),
                match.reasons()
        );
    }

    private PublicationResponse toPublicationResponse(Publication publication) {

        return new PublicationResponse(
                publication.getPublicationId(),
                publication.getUserId(),
                publication.getCategoryId(),
                publication.getPublicationType(),
                publication.getTitle(),
                publication.getDescription(),
                publication.getInteractionMode(),
                publication.getPheltAmount(),
                publication.isNegotiable(),
                publication.getPublicationStatus(),
                publication.getDurationMinutes()
        );
    }
}
