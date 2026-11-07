package com.phelti.phelti.service;

import com.phelti.phelti.entity.WalletEntity;

public interface WalletService {

    WalletEntity createWallet(Integer userId);

    WalletEntity findWalletByUserId(Integer userId);
}