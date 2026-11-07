package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.exception.WalletAlreadyExistsException;
import com.phelti.phelti.exception.WalletNotFoundException;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import com.phelti.phelti.repository.jpa.WalletJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletJpaRepository walletRepository;

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWalletShouldCreateWalletWhenUserExists() {

        Integer userId = 1;

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        when(walletRepository.existsByUserUserId(userId))
                .thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(walletRepository.save(any(WalletEntity.class)))
                .thenAnswer(invocation -> {
                    WalletEntity wallet = invocation.getArgument(0);
                    wallet.setWalletId(1);
                    return wallet;
                });

        WalletEntity result = walletService.createWallet(userId);

        assertNotNull(result);
        assertEquals(1, result.getWalletId());
        assertEquals(userId, result.getUser().getUserId());
        assertEquals(BigDecimal.ZERO, result.getAvailableBalance());
        assertEquals(BigDecimal.ZERO, result.getReservedBalance());

        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    void createWalletShouldThrowExceptionWhenWalletAlreadyExists() {

        Integer userId = 1;

        when(walletRepository.existsByUserUserId(userId))
                .thenReturn(true);

        assertThrows(
                WalletAlreadyExistsException.class,
                () -> walletService.createWallet(userId)
        );

        verify(userRepository, never()).findById(userId);
        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void createWalletShouldThrowExceptionWhenUserDoesNotExist() {

        Integer userId = 999;

        when(walletRepository.existsByUserUserId(userId))
                .thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.createWallet(userId)
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void findWalletByUserIdShouldReturnWalletWhenItExists() {

        Integer userId = 1;

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        WalletEntity wallet = new WalletEntity();
        wallet.setWalletId(1);
        wallet.setUser(user);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setReservedBalance(BigDecimal.ZERO);

        when(walletRepository.findByUserUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletEntity result = walletService.findWalletByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.getWalletId());
        assertEquals(userId, result.getUser().getUserId());
    }

    @Test
    void findWalletByUserIdShouldThrowExceptionWhenWalletDoesNotExist() {

        Integer userId = 999;

        when(walletRepository.findByUserUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.findWalletByUserId(userId)
        );
    }
}
