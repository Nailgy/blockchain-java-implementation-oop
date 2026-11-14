package com.nali.blockchain.service;

import com.nali.blockchain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Rules about the miner reward that every block must follow.
 * They are in one place because two classes need exactly the same rule:
 * BlockchainService (to create the reward) and ChainValidator (to check it).
 */
public final class ConsensusRules {

    public static final BigDecimal MINING_REWARD = new BigDecimal("50");

    private ConsensusRules() {
    }

    /**
     * The reward a miner is allowed to take: the fixed reward plus the fees of all
     * the other transactions of the block.
     * "? extends Transaction" accepts a List<Transaction> as well as a List<TransferTransaction>.
     */
    public static BigDecimal calculateReward(List<? extends Transaction> transactions) {
        BigDecimal totalFees = transactions.stream()
                .map(Transaction::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MINING_REWARD.add(totalFees);
    }
}
