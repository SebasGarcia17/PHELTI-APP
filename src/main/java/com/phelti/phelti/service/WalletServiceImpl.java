package com.phelti.phelti.service;

import com.phelti.phelti.entity.TransactionEntity;
import com.phelti.phelti.entity.UserEntity;
import com.phelti.phelti.entity.WalletEntity;
import com.phelti.phelti.exception.InsufficientBalanceException;
import com.phelti.phelti.exception.InvalidWalletOperationException;
import com.phelti.phelti.exception.UserNotFoundException;
import com.phelti.phelti.exception.WalletAlreadyExistsException;
import com.phelti.phelti.exception.WalletNotFoundException;
import com.phelti.phelti.model.TransactionType;
import com.phelti.phelti.repository.jpa.TransactionJpaRepository;
import com.phelti.phelti.repository.jpa.UserJpaRepository;
import com.phelti.phelti.repository.jpa.WalletJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletJpaRepository walletRepository;
    private final UserJpaRepository userRepository;
    private final TransactionJpaRepository transactionRepository;

    public WalletServiceImpl(
            WalletJpaRepository walletRepository,
            UserJpaRepository userRepository,
            TransactionJpaRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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

        WalletEntity saved = walletRepository.save(wallet);

        recordTransaction(
                wallet,
                TransactionType.DEPOSIT,
                amount,
                "Phelts deposited into the wallet",
                null,
                null,
                null
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionEntity> findTransactionsByUserId(Integer userId) {

        findWalletByUserId(userId);

        return transactionRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void reserveBalance(Integer userId, BigDecimal amount, Integer activityId) {

        validatePositiveAmount(amount);

        WalletEntity wallet = findWalletForUpdate(userId);

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(userId);
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setReservedBalance(wallet.getReservedBalance().add(amount));

        walletRepository.save(wallet);

        recordTransaction(
                wallet,
                TransactionType.RESERVE,
                amount,
                "Phelts reserved for a scheduled activity",
                activityId,
                null,
                null
        );
    }

    @Override
    @Transactional
    public void releaseReservedBalance(Integer userId, BigDecimal amount, Integer activityId) {

        validatePositiveAmount(amount);

        WalletEntity wallet = findWalletForUpdate(userId);
        ensureReservedBalance(wallet, amount);

        wallet.setReservedBalance(wallet.getReservedBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

        walletRepository.save(wallet);

        recordTransaction(
                wallet,
                TransactionType.RELEASE,
                amount,
                "Reserved phelts released back to the available balance",
                activityId,
                null,
                null
        );
    }

    @Override
    @Transactional
    public void transferReservedBalance(
            Integer fromUserId,
            Integer toUserId,
            BigDecimal amount,
            Integer activityId) {

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

        recordTransaction(
                source,
                TransactionType.TRANSFER_OUT,
                amount,
                "Phelts paid to user " + toUserId + " for a completed activity",
                activityId,
                fromUserId,
                toUserId
        );

        recordTransaction(
                destination,
                TransactionType.TRANSFER_IN,
                amount,
                "Phelts received from user " + fromUserId + " for a completed activity",
                activityId,
                fromUserId,
                toUserId
        );
    }

    private void recordTransaction(
            WalletEntity wallet,
            TransactionType type,
            BigDecimal amount,
            String description,
            Integer activityId,
            Integer originUserId,
            Integer destinationUserId) {

        TransactionEntity transaction = new TransactionEntity();
        transaction.setWallet(wallet);
        transaction.setTransactionType(type.name());
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setActivityId(activityId);
        transaction.setOriginUserId(originUserId);
        transaction.setDestinationUserId(destinationUserId);

        transactionRepository.save(transaction);
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
