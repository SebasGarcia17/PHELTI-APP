package com.phelti.phelti.service;

import com.phelti.phelti.entity.TransactionEntity;
import com.phelti.phelti.entity.WalletEntity;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    WalletEntity createWallet(Integer userId);

    WalletEntity findWalletByUserId(Integer userId);

    WalletEntity deposit(Integer userId, BigDecimal amount);

    List<TransactionEntity> findTransactionsByUserId(Integer userId);

    void reserveBalance(Integer userId, BigDecimal amount, Integer activityId);

    void releaseReservedBalance(Integer userId, BigDecimal amount, Integer activityId);

    void transferReservedBalance(Integer fromUserId, Integer toUserId, BigDecimal amount, Integer activityId);
}
