package com.nali.blockchain.service;

import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.RewardTransaction;
import com.nali.blockchain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProofOfWorkTest {

    @Test
    void minedBlockHashShouldStartWithRequiredZeros() {
        ProofOfWork proofOfWork = new ProofOfWork(2);
        List<Transaction> transactions = List.of(new RewardTransaction("MINER-99", new BigDecimal("50")));

        Block block = proofOfWork.mineBlock(0, Block.GENESIS_PREVIOUS_HASH, transactions);

        assertTrue(block.getHash().startsWith("00"));
        assertEquals(0, block.getIndex());
    }

    @Test
    void difficultyZeroShouldAcceptAnyHash() {
        assertTrue(new ProofOfWork(0).isHashValid("abc"));
    }

    @Test
    void shouldRejectHashWithoutEnoughZeros() {
        assertFalse(new ProofOfWork(3).isHashValid("00abc"));
    }

    @Test
    void shouldThrowExceptionWhenDifficultyIsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new ProofOfWork(-1));
        assertThrows(IllegalArgumentException.class, () -> new ProofOfWork(7));
    }
}
