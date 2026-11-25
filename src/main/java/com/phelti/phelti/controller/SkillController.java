package com.phelti.phelti.controller;

import com.phelti.phelti.dto.SkillResponse;
import com.phelti.phelti.entity.SkillEntity;
import com.phelti.phelti.service.SkillService;
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
@Tag(name = "Skills", description = "Skills a user can offer to others")
public class SkillController {

    private final com.phelti.phelti.config.DemoAccess access;

    private final SkillService skillService;

    public SkillController(com.phelti.phelti.config.DemoAccess access, SkillService skillService) {
        this.access = access;
        this.skillService = skillService;
    }

    @Operation(summary = "List the skills of the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skills listed")
    })
    @GetMapping("/skills")
    public ResponseEntity<List<SkillResponse>> findAllSkills() {

        return ResponseEntity.ok(toResponses(skillService.findAllSkills()));
    }

    @Operation(summary = "List the skills of a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skills listed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<List<SkillResponse>> findSkillsByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        return ResponseEntity.ok(toResponses(skillService.findSkillsByUserId(userId)));
    }

    @Operation(summary = "Assign a skill to a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Skill assigned"),
            @ApiResponse(responseCode = "404", description = "User or skill not found"),
            @ApiResponse(responseCode = "409", description = "Skill already assigned to the user")
    })
    @PutMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<Void> addSkillToUser(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Parameter(description = "Skill identifier", example = "1")
            @PathVariable Integer skillId) {
        access.own(userId);

        skillService.addSkillToUser(userId, skillId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a skill from a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Skill removed"),
            @ApiResponse(responseCode = "404", description = "User, skill or assignment not found")
    })
    @DeleteMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<Void> removeSkillFromUser(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Parameter(description = "Skill identifier", example = "1")
            @PathVariable Integer skillId) {
        access.own(userId);

        skillService.removeSkillFromUser(userId, skillId);

        return ResponseEntity.noContent().build();
    }

    private List<SkillResponse> toResponses(List<SkillEntity> skills) {

        return skills.stream()
                .map(this::toResponse)
                .toList();
    }

    private SkillResponse toResponse(SkillEntity skill) {

        return new SkillResponse(skill.getSkillId(), skill.getName());
    }
}
