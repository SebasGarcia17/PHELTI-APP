package com.phelti.phelti.service;

import com.phelti.phelti.entity.WalletEntity;

import java.math.BigDecimal;

public interface WalletService {

    WalletEntity createWallet(Integer userId);

    WalletEntity findWalletByUserId(Integer userId);

    WalletEntity deposit(Integer userId, BigDecimal amount);

    void reserveBalance(Integer userId, BigDecimal amount);

    void releaseReservedBalance(Integer userId, BigDecimal amount);

    void transferReservedBalance(Integer fromUserId, Integer toUserId, BigDecimal amount);
}
