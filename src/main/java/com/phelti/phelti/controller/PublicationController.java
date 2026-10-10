package com.phelti.phelti.controller;

import com.phelti.phelti.model.Publication;
import com.phelti.phelti.service.PublicationService;
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
@RequestMapping("/api/publications")
@Tag(name = "Publications", description = "Operations for time-exchange publications")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Operation(summary = "Create a publication")
    @PostMapping
    public ResponseEntity<Publication> createPublication(@RequestBody Publication publication) {
        Publication created = publicationService.createPublication(publication);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all publications")
    @GetMapping
    public ResponseEntity<List<Publication>> findAllPublications() {
        return ResponseEntity.ok(publicationService.findAllPublications());
    }

    @Operation(summary = "Get a publication by id")
    @GetMapping("/{id}")
    public ResponseEntity<Publication> findPublicationById(@PathVariable("id") Integer publicationId) {
        return ResponseEntity.ok(publicationService.findPublicationById(publicationId));
    }

    @Operation(summary = "Update a publication")
    @PutMapping("/{id}")
    public ResponseEntity<Publication> updatePublication(@PathVariable("id") Integer publicationId,
                                                           @RequestBody Publication publication) {
        return ResponseEntity.ok(publicationService.updatePublication(publicationId, publication));
    }

    @Operation(summary = "Delete a publication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable("id") Integer publicationId) {
        publicationService.deletePublication(publicationId);
        return ResponseEntity.noContent().build();
    }
}
