package com.phelti.phelti.controller;

import com.phelti.phelti.dto.ProfileUpdateRequest;
import com.phelti.phelti.dto.UserRequest;
import com.phelti.phelti.dto.UserResponse;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations for platform users")
public class UserController {

    private final com.phelti.phelti.config.DemoAccess access;

    private final UserService userService;

    public UserController(com.phelti.phelti.config.DemoAccess access, UserService userService) {
        this.access = access;
        this.userService = userService;
    }

    @Operation(summary = "Register a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        access.requireGateway();

        UserEntity user = userService.createUser(
                request.name(),
                request.email(),
                request.description()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @Operation(summary = "List all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users listed")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {

        List<UserResponse> users = userService.findAllUsers().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get a user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable("id") Integer userId) {

        return ResponseEntity.ok(toResponse(userService.findUserById(userId)));
    }

    @Operation(
            summary = "Update the profile of a user",
            description = "Updates name, description, location and preferred interaction mode"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable("id") Integer userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        access.own(userId);

        UserEntity user = userService.updateUserProfile(
                userId,
                request.name(),
                request.description(),
                request.location(),
                request.preferredInteractionMode()
        );

        return ResponseEntity.ok(toResponse(user));
    }

    @Operation(
            summary = "Search users",
            description = "Filters users by skill, interest, preferred interaction mode and day of availability. All filters are optional and combined"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value")
    })
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Skill identifier", example = "1")
            @RequestParam(required = false) Integer skillId,
            @Parameter(description = "Interest identifier", example = "1")
            @RequestParam(required = false) Integer interestId,
            @Parameter(description = "Preferred interaction mode", example = "VIRTUAL")
            @RequestParam(required = false) String interactionMode,
            @Parameter(description = "Day of the week with availability", example = "SATURDAY")
            @RequestParam(required = false) String dayOfWeek) {

        List<UserResponse> users = userService
                .searchUsers(skillId, interestId, interactionMode, dayOfWeek)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    private UserResponse toResponse(UserEntity user) {

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getUserId().equals(access.optionalActor()) ? user.getEmail() : null,
                user.getDescription(),
                user.getAverageRating(),
                user.getLocation(),
                user.getPreferredInteractionMode()
        );
    }
}
