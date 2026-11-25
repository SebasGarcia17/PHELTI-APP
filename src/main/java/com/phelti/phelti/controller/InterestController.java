package com.phelti.phelti.controller;

import com.phelti.phelti.dto.InterestResponse;
import com.phelti.phelti.entity.InterestEntity;
import com.phelti.phelti.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Interests", description = "Interests of the users, used for search and matching")
public class InterestController {

    private final com.phelti.phelti.config.DemoAccess access;

    private final InterestService interestService;

    public InterestController(com.phelti.phelti.config.DemoAccess access, InterestService interestService) {
        this.access = access;
        this.interestService = interestService;
    }

    @Operation(summary = "List the interests of the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interests listed")
    })
    @GetMapping("/interests")
    public ResponseEntity<List<InterestResponse>> findAllInterests() {

        return ResponseEntity.ok(toResponses(interestService.findAllInterests()));
    }

    @Operation(summary = "List the interests of a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interests listed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/interests")
    public ResponseEntity<List<InterestResponse>> findInterestsByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        return ResponseEntity.ok(toResponses(interestService.findInterestsByUserId(userId)));
    }

    @Operation(summary = "Assign a interest to a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Interest assigned"),
            @ApiResponse(responseCode = "404", description = "User or interest not found"),
            @ApiResponse(responseCode = "409", description = "Interest already assigned to the user")
    })
    @PutMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> addInterestToUser(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Parameter(description = "Interest identifier", example = "1")
            @PathVariable Integer interestId) {
        access.own(userId);

        interestService.addInterestToUser(userId, interestId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a interest from a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Interest removed"),
            @ApiResponse(responseCode = "404", description = "User, interest or assignment not found")
    })
    @DeleteMapping("/users/{userId}/interests/{interestId}")
    public ResponseEntity<Void> removeInterestFromUser(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Parameter(description = "Interest identifier", example = "1")
            @PathVariable Integer interestId) {
        access.own(userId);

        interestService.removeInterestFromUser(userId, interestId);

        return ResponseEntity.noContent().build();
    }

    private List<InterestResponse> toResponses(List<InterestEntity> interests) {

        return interests.stream()
                .map(this::toResponse)
                .toList();
    }

    private InterestResponse toResponse(InterestEntity interest) {

        return new InterestResponse(interest.getInterestId(), interest.getName());
    }
}
