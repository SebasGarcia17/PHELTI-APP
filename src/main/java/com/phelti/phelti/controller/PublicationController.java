package com.phelti.phelti.controller;

import com.phelti.phelti.dto.PublicationRequest;
import com.phelti.phelti.dto.PublicationResponse;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.service.PublicationService;
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
@RequestMapping("/api/publications")
@Tag(name = "Publications", description = "Operations for time-exchange publications")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Operation(
            summary = "Create a publication",
            description = "Publishes an offer or a request for support. The status defaults to PUBLISHED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publication created"),
            @ApiResponse(responseCode = "400", description = "Invalid publication data"),
            @ApiResponse(responseCode = "409", description = "The user or the category does not exist")
    })
    @PostMapping
    public ResponseEntity<PublicationResponse> createPublication(
            @Valid @RequestBody PublicationRequest request) {
        Publication created = publicationService.createPublication(toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(summary = "List all publications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publications listed")
    })
    @GetMapping
    public ResponseEntity<List<PublicationResponse>> findAllPublications() {
        List<PublicationResponse> publications = publicationService.findAllPublications()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(publications);
    }

    @Operation(summary = "Get a publication by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publication found"),
            @ApiResponse(responseCode = "404", description = "Publication not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponse> findPublicationById(
            @Parameter(description = "Publication identifier", example = "1")
            @PathVariable("id") Integer publicationId) {
        return ResponseEntity.ok(toResponse(publicationService.findPublicationById(publicationId)));
    }

    @Operation(
            summary = "Update a publication",
            description = "Replaces every field of the publication. A blank status keeps the current one"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publication updated"),
            @ApiResponse(responseCode = "400", description = "Invalid publication data"),
            @ApiResponse(responseCode = "404", description = "Publication not found"),
            @ApiResponse(responseCode = "409", description = "The user or the category does not exist")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PublicationResponse> updatePublication(
            @Parameter(description = "Publication identifier", example = "1")
            @PathVariable("id") Integer publicationId,
            @Valid @RequestBody PublicationRequest request) {
        Publication updated = publicationService.updatePublication(publicationId, toModel(request));
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Delete a publication",
            description = "Also deletes its proposals. Fails when an activity already references it"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publication deleted"),
            @ApiResponse(responseCode = "404", description = "Publication not found"),
            @ApiResponse(responseCode = "409", description = "The publication is referenced by an activity")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(
            @Parameter(description = "Publication identifier", example = "1")
            @PathVariable("id") Integer publicationId) {
        publicationService.deletePublication(publicationId);
        return ResponseEntity.noContent().build();
    }

    private Publication toModel(PublicationRequest request) {
        Publication publication = new Publication();
        publication.setUserId(request.userId());
        publication.setCategoryId(request.categoryId());
        publication.setPublicationType(request.publicationType());
        publication.setTitle(request.title());
        publication.setDescription(request.description());
        publication.setInteractionMode(request.interactionMode());
        publication.setPheltAmount(request.pheltAmount());
        publication.setNegotiable(request.negotiable());
        publication.setPublicationStatus(request.publicationStatus());
        return publication;
    }

    private PublicationResponse toResponse(Publication publication) {
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
                publication.getPublicationStatus()
        );
    }
}
