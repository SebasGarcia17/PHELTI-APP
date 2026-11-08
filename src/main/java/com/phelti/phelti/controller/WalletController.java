package com.phelti.phelti.controller;

import com.phelti.phelti.dto.DepositRequest;
import com.phelti.phelti.dto.WalletResponse;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.service.WalletService;

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

@RestController
@RequestMapping("/api/wallets")
@Tag(
        name = "Wallets",
        description = "Operations for user wallets and Phelt balances"
)
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a wallet",
            description = "Creates a wallet for an existing user with zero initial balance"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already has a wallet")
    })
    @PostMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> createWallet(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        WalletEntity wallet = walletService.createWallet(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(wallet));
    }

    @Operation(
            summary = "Get wallet by user",
            description = "Returns the wallet and current balances associated with a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet found successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> findWalletByUserId(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId) {

        WalletEntity wallet = walletService.findWalletByUserId(userId);

        return ResponseEntity.ok(toResponse(wallet));
    }

    @Operation(
            summary = "Deposit phelts",
            description = "Adds phelts to the available balance of the user's wallet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit applied"),
            @ApiResponse(responseCode = "400", description = "Amount must be greater than zero"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/users/{userId}/deposits")
    public ResponseEntity<WalletResponse> deposit(
            @Parameter(description = "User identifier", example = "1")
            @PathVariable Integer userId,
            @Valid @RequestBody DepositRequest request) {

        WalletEntity wallet = walletService.deposit(userId, request.amount());

        return ResponseEntity.ok(toResponse(wallet));
    }

    private WalletResponse toResponse(WalletEntity wallet) {

        return new WalletResponse(
                wallet.getWalletId(),
                wallet.getUser().getUserId(),
                wallet.getAvailableBalance(),
                wallet.getReservedBalance()
        );
    }
}
