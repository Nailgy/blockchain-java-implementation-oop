package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Checks that a list of blocks is a valid chain:
 * correct order, correct links between blocks, valid proof-of-work, correct miner rewards,
 * and transactions that are valid when replayed from the beginning.
 */
public class ChainValidator {

    private final MiningStrategy miningStrategy;

    public ChainValidator(MiningStrategy miningStrategy) {
        this.miningStrategy = miningStrategy;
    }

    public void validate(List<Block> blocks) throws InvalidChainException {
        if (blocks.isEmpty()) {
            throw new InvalidChainException("The chain has no blocks");
        }

        // Replaying every transaction on an empty ledger must never fail.
        Ledger replayLedger = new Ledger();

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            String expectedPreviousHash = (i == 0) ? Block.GENESIS_PREVIOUS_HASH : blocks.get(i - 1).getHash();

            if (block.getIndex() != i) {
                throw new InvalidChainException("Block at position " + i + " has index " + block.getIndex());
            }
            if (!block.getPreviousHash().equals(expectedPreviousHash)) {
                throw new InvalidChainException("Block #" + i + " does not point to the previous block");
            }
            if (!miningStrategy.isHashValid(block.getHash())) {
                throw new InvalidChainException("Block #" + i + " has an invalid proof-of-work");
            }
            checkRewards(block);
            for (Transaction transaction : block.getTransactions()) {
                try {
                    transaction.validate();
                    transaction.applyTo(replayLedger);
                } catch (BlockchainException e) {
                    throw new InvalidChainException("Block #" + i + " has an invalid transaction: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Without this check, an attacker could mine a block that pays them any amount of new coins.
     */
    private void checkRewards(Block block) throws InvalidChainException {
        List<Transaction> transactions = block.getTransactions();

        if (block.getIndex() == 0) {
            // The genesis block only creates the initial balances, so it contains only rewards.
            for (Transaction transaction : transactions) {
                if (transaction.getType() != TransactionType.REWARD) {
                    throw new InvalidChainException("Genesis block must contain only reward transactions");
                }
            }
            return;
        }

        if (transactions.isEmpty() || transactions.getLast().getType() != TransactionType.REWARD) {
            throw new InvalidChainException("Block #" + block.getIndex() + " must end with the miner reward");
        }
        List<Transaction> otherTransactions = transactions.subList(0, transactions.size() - 1);
        for (Transaction transaction : otherTransactions) {
            if (transaction.getType() == TransactionType.REWARD) {
                throw new InvalidChainException("Block #" + block.getIndex() + " has more than one reward");
            }
        }

        BigDecimal expectedReward = ConsensusRules.calculateReward(otherTransactions);
        BigDecimal actualReward = transactions.getLast().getAmount();
        if (actualReward.compareTo(expectedReward) != 0) {
            throw new InvalidChainException("Block #" + block.getIndex() + " has a reward of " + actualReward
                    + " but the allowed reward is " + expectedReward);
        }
    }
}
