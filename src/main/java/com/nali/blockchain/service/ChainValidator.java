package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.Transaction;

import java.util.List;

/**
 * Checks that a list of blocks is a valid chain:
 * correct order, correct links between blocks, valid proof-of-work,
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
}
