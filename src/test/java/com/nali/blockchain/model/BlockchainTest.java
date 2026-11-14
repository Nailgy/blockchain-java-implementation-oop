package com.nali.blockchain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockchainTest {

    private final LocalDateTime timestamp = LocalDateTime.of(2026, 11, 15, 12, 0);
    private Blockchain blockchain;
    private Block genesis;

    @BeforeEach
    void setUp() {
        blockchain = new Blockchain();
        genesis = new Block(0, Block.GENESIS_PREVIOUS_HASH, timestamp, List.of(), 0);
        blockchain.addBlock(genesis);
    }

    @Test
    void shouldAddBlockThatPointsToTheLastBlock() {
        Block second = new Block(1, genesis.getHash(), timestamp, List.of(), 0);

        blockchain.addBlock(second);

        assertEquals(2, blockchain.size());
        assertEquals(second, blockchain.getLatestBlock());
    }

    @Test
    void shouldRejectBlockWithWrongPreviousHash() {
        Block second = new Block(1, "wrong-hash", timestamp, List.of(), 0);

        assertThrows(IllegalArgumentException.class, () -> blockchain.addBlock(second));
    }

    @Test
    void shouldRejectBlockWithWrongIndex() {
        Block second = new Block(2, genesis.getHash(), timestamp, List.of(), 0);

        assertThrows(IllegalArgumentException.class, () -> blockchain.addBlock(second));
    }

    @Test
    void emptyChainShouldHaveNoLatestBlock() {
        assertThrows(IllegalStateException.class, () -> new Blockchain().getLatestBlock());
    }
}
