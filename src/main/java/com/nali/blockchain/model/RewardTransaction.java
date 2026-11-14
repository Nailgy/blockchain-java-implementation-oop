package com.nali.blockchain.model;

import java.math.BigDecimal;

/**
 * New coins created by the network. It has no real sender.
 * Used for the mining reward and for the initial balances in the first (genesis) block.
 */
public class RewardTransaction extends Transaction {

    public static final String NETWORK_SENDER = "NETWORK";

    public RewardTransaction(String receiver, BigDecimal amount) {
        super(receiver, amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.REWARD;
    }

    @Override
    public String getSender() {
        return NETWORK_SENDER;
    }

    // Nobody pays for a reward, so this can never fail.
    @Override
    public void applyTo(Ledger ledger) {
        ledger.credit(getReceiver(), getAmount());
    }
}
