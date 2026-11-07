package com.phelti.phelti.controller;

import com.phelti.phelti.dto.WalletResponse;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> createWallet(
            @PathVariable Integer userId) {

        WalletEntity wallet = walletService.createWallet(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(wallet));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<WalletResponse> findWalletByUserId(
            @PathVariable Integer userId) {

        WalletEntity wallet = walletService.findWalletByUserId(userId);

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