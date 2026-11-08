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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public WalletEntity createWallet(Integer userId) {

        if (walletRepository.existsByUserUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        WalletEntity wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setReservedBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletEntity findWalletByUserId(Integer userId) {

        return walletRepository.findByUserUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    @Override
    @Transactional
    public WalletEntity deposit(Integer userId, BigDecimal amount) {

        validatePositiveAmount(amount);

        WalletEntity wallet = findWalletForUpdate(userId);
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void reserveBalance(Integer userId, BigDecimal amount) {

        validatePositiveAmount(amount);

        WalletEntity wallet = findWalletForUpdate(userId);

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(userId);
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setReservedBalance(wallet.getReservedBalance().add(amount));

        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void releaseReservedBalance(Integer userId, BigDecimal amount) {

        validatePositiveAmount(amount);

        WalletEntity wallet = findWalletForUpdate(userId);
        ensureReservedBalance(wallet, amount);

        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void transferReservedBalance(
            Integer fromUserId,
            Integer toUserId,
            BigDecimal amount) {

        validatePositiveAmount(amount);

        if (fromUserId.equals(toUserId)) {
            throw new InvalidWalletOperationException(
                    "source and destination wallets must be different"
            );
        }

        WalletEntity source = findWalletForUpdate(fromUserId);
        WalletEntity destination = findWalletForUpdate(toUserId);
        ensureReservedBalance(source, amount);

        source.setReservedBalance(source.getReservedBalance().subtract(amount));
        destination.setAvailableBalance(destination.getAvailableBalance().add(amount));

        walletRepository.save(source);
        walletRepository.save(destination);
    }

    private WalletEntity findWalletForUpdate(Integer userId) {

        return walletRepository.findForUpdateByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    private void validatePositiveAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWalletOperationException(
                    "amount must be greater than zero"
            );
        }
    }

    private void ensureReservedBalance(WalletEntity wallet, BigDecimal amount) {

        if (wallet.getReservedBalance().compareTo(amount) < 0) {
            throw new InvalidWalletOperationException(
                    "reserved balance is lower than the requested amount"
            );
        }
    }
}
