package com.phelti.phelti.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations for platform users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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

    private UserResponse toResponse(UserEntity user) {

        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getDescription(),
                user.getAverageRating()
        );
    }
}
