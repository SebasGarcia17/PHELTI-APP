package com.phelti.phelti.service;

import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.exception.InsufficientBalanceException;
import com.phelti.phelti.exception.InvalidWalletOperationException;
import com.phelti.phelti.exception.UserNotFoundException;
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
        UserEntity user = createUser(userId);

        when(walletRepository.existsByUserUserId(userId))
                .thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(walletRepository.save(any(WalletEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletEntity result = walletService.createWallet(userId);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAvailableBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getReservedBalance()));

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
                UserNotFoundException.class,
                () -> walletService.createWallet(userId)
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void findWalletByUserIdShouldReturnWalletWhenItExists() {

        Integer userId = 1;
        WalletEntity wallet = createWallet(userId, "10.00", "0.00");

        when(walletRepository.findByUserUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletEntity result = walletService.findWalletByUserId(userId);

        assertEquals(wallet, result);
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

    @Test
    void depositShouldIncreaseAvailableBalance() {

        WalletEntity wallet = createWallet(1, "10.00", "0.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        WalletEntity result = walletService.deposit(1, new BigDecimal("15.50"));

        assertEquals(0, new BigDecimal("25.50").compareTo(result.getAvailableBalance()));
        verify(walletRepository).save(wallet);
    }

    @Test
    void depositShouldThrowExceptionWhenAmountIsNotPositive() {

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.deposit(1, BigDecimal.ZERO)
        );

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.deposit(1, null)
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void depositShouldThrowExceptionWhenWalletDoesNotExist() {

        when(walletRepository.findForUpdateByUserId(999))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.deposit(999, BigDecimal.TEN)
        );
    }

    @Test
    void reserveBalanceShouldMoveAmountFromAvailableToReserved() {

        WalletEntity wallet = createWallet(1, "30.00", "5.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(wallet));

        walletService.reserveBalance(1, new BigDecimal("20.00"));

        assertEquals(0, new BigDecimal("10.00").compareTo(wallet.getAvailableBalance()));
        assertEquals(0, new BigDecimal("25.00").compareTo(wallet.getReservedBalance()));
        verify(walletRepository).save(wallet);
    }

    @Test
    void reserveBalanceShouldThrowExceptionWhenBalanceIsInsufficient() {

        WalletEntity wallet = createWallet(1, "10.00", "0.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> walletService.reserveBalance(1, new BigDecimal("20.00"))
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void reserveBalanceShouldThrowExceptionWhenAmountIsNotPositive() {

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.reserveBalance(1, new BigDecimal("-1"))
        );
    }

    @Test
    void releaseReservedBalanceShouldMoveAmountBackToAvailable() {

        WalletEntity wallet = createWallet(1, "10.00", "20.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(wallet));

        walletService.releaseReservedBalance(1, new BigDecimal("20.00"));

        assertEquals(0, new BigDecimal("30.00").compareTo(wallet.getAvailableBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getReservedBalance()));
        verify(walletRepository).save(wallet);
    }

    @Test
    void releaseReservedBalanceShouldThrowExceptionWhenReservedBalanceIsLower() {

        WalletEntity wallet = createWallet(1, "10.00", "5.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.releaseReservedBalance(1, new BigDecimal("20.00"))
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void transferReservedBalanceShouldMoveReservedPhelstToDestinationAvailable() {

        WalletEntity source = createWallet(1, "0.00", "20.00");
        WalletEntity destination = createWallet(2, "5.00", "0.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(source));

        when(walletRepository.findForUpdateByUserId(2))
                .thenReturn(Optional.of(destination));

        walletService.transferReservedBalance(1, 2, new BigDecimal("20.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(source.getReservedBalance()));
        assertEquals(0, new BigDecimal("25.00").compareTo(destination.getAvailableBalance()));
        verify(walletRepository).save(source);
        verify(walletRepository).save(destination);
    }

    @Test
    void transferReservedBalanceShouldThrowExceptionWhenWalletsAreTheSame() {

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.transferReservedBalance(1, 1, BigDecimal.TEN)
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    @Test
    void transferReservedBalanceShouldThrowExceptionWhenReservedBalanceIsLower() {

        WalletEntity source = createWallet(1, "0.00", "5.00");
        WalletEntity destination = createWallet(2, "0.00", "0.00");

        when(walletRepository.findForUpdateByUserId(1))
                .thenReturn(Optional.of(source));

        when(walletRepository.findForUpdateByUserId(2))
                .thenReturn(Optional.of(destination));

        assertThrows(
                InvalidWalletOperationException.class,
                () -> walletService.transferReservedBalance(1, 2, new BigDecimal("20.00"))
        );

        verify(walletRepository, never()).save(any(WalletEntity.class));
    }

    private UserEntity createUser(Integer userId) {

        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setName("Test user");
        user.setEmail("user" + userId + "@example.com");

        return user;
    }

    private WalletEntity createWallet(Integer userId, String available, String reserved) {

        WalletEntity wallet = new WalletEntity();
        wallet.setUser(createUser(userId));
        wallet.setAvailableBalance(new BigDecimal(available));
        wallet.setReservedBalance(new BigDecimal(reserved));

        return wallet;
    }
}
