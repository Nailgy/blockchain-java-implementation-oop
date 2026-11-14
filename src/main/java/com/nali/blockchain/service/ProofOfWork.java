package com.nali.blockchain.service;

import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Proof-of-work: try nonce = 0, 1, 2, ... until the hash of the block
 * starts with a given number of zeros (the difficulty).
 * Each extra zero makes mining about 16 times slower.
 */
public class ProofOfWork implements MiningStrategy {

    private static final int MAX_DIFFICULTY = 6;

    private final String requiredPrefix;

    public ProofOfWork(int difficulty) {
        if (difficulty < 0 || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException("Difficulty must be between 0 and " + MAX_DIFFICULTY
                    + ", but was " + difficulty);
        }
        this.requiredPrefix = "0".repeat(difficulty);
    }

    @Override
    public Block mineBlock(int index, String previousHash, List<Transaction> transactions) {
        LocalDateTime timestamp = LocalDateTime.now();
        long nonce = 0;
        Block block = new Block(index, previousHash, timestamp, transactions, nonce);
        while (!isHashValid(block.getHash())) {
            nonce++;
            block = new Block(index, previousHash, timestamp, transactions, nonce);
        }
        return block;
    }

    @Override
    public boolean isHashValid(String hash) {
        return hash.startsWith(requiredPrefix);
    }
}
