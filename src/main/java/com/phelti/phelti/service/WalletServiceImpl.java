package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.exception.WalletAlreadyExistsException;
import com.phelti.phelti.exception.WalletNotFoundException;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import com.phelti.phelti.repository.jpa.WalletJpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletJpaRepository walletRepository;
    private final UserJpaRepository userRepository;

    public WalletServiceImpl(
            WalletJpaRepository walletRepository,
            UserJpaRepository userRepository) {

        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WalletEntity createWallet(Integer userId) {

        if (walletRepository.existsByUserUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );

        WalletEntity wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setReservedBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    @Override
    public WalletEntity findWalletByUserId(Integer userId) {

        return walletRepository.findByUserUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }
}