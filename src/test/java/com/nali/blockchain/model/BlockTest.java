package com.nali.blockchain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTest {

    private final LocalDateTime timestamp = LocalDateTime.of(2026, 11, 15, 12, 0);
    private final List<Transaction> transactions = List.of(new RewardTransaction("ALICE-01", new BigDecimal("100")));

    @Test
    void sameDataShouldGiveSameHash() {
        Block first = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, transactions, 7);
        Block second = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, transactions, 7);

        assertEquals(first.getHash(), second.getHash());
    }

    @Test
    void differentNonceShouldGiveDifferentHash() {
        Block first = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, transactions, 7);
        Block second = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, transactions, 8);

        assertNotEquals(first.getHash(), second.getHash());
    }

    @Test
    void transactionsOfBlockShouldNotChangeWhenOriginalListChanges() {
        List<Transaction> list = new ArrayList<>(transactions);
        Block block = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, list, 0);

        list.add(new RewardTransaction("ATTACKER", new BigDecimal("1000")));

        assertEquals(1, block.getTransactions().size());
        assertThrows(UnsupportedOperationException.class, () -> block.getTransactions().clear());
    }

    @Test
    void shouldRejectNegativeIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> new Block(-1, Block.GENESIS_PREVIOUS_HASH, timestamp, transactions, 0));
    }
}
