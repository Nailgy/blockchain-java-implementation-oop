package com.nali.blockchain.model;

import com.nali.blockchain.util.HashUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * A block of the chain. A block cannot be changed after it is created:
 * all fields are final and the list of transactions is a copy that cannot be modified.
 * Its hash is calculated from all its data, so any change would give a different hash.
 */
public final class Block {

    // The first block has no previous block, so it points to this value instead.
    public static final String GENESIS_PREVIOUS_HASH = "0";

    private final int index;
    private final String previousHash;
    private final LocalDateTime timestamp;
    private final List<Transaction> transactions;
    private final long nonce;
    private final String hash;

    public Block(int index, String previousHash, LocalDateTime timestamp,
                 List<Transaction> transactions, long nonce) {
        if (index < 0) {
            throw new IllegalArgumentException("Block index must not be negative: " + index);
        }
        this.index = index;
        this.previousHash = Objects.requireNonNull(previousHash, "previousHash must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.transactions = List.copyOf(transactions);
        this.nonce = nonce;
        this.hash = calculateHash();
    }

    private String calculateHash() {
        StringBuilder data = new StringBuilder();
        data.append(index).append('|')
                .append(previousHash).append('|')
                .append(timestamp).append('|')
                .append(nonce);
        for (Transaction transaction : transactions) {
            data.append('|').append(transaction.toDataString());
        }
        return HashUtil.sha256(data.toString());
    }

    public int getIndex() {
        return index;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "Block #" + index + " | hash " + hash + " | previous " + previousHash
                + " | nonce " + nonce + " | " + transactions.size() + " transaction(s)";
    }
}
