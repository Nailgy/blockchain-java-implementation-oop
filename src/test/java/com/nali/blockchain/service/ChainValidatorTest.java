package com.nali.blockchain.service;

import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.RewardTransaction;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransferTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChainValidatorTest {

    private ProofOfWork proofOfWork;
    private ChainValidator validator;
    private Block genesis;

    @BeforeEach
    void setUp() {
        proofOfWork = new ProofOfWork(1);
        validator = new ChainValidator(proofOfWork);
        List<Transaction> allocations = List.of(new RewardTransaction("ALICE-01", new BigDecimal("100")));
        genesis = proofOfWork.mineBlock(0, Block.GENESIS_PREVIOUS_HASH, allocations);
    }

    @Test
    void validChainShouldPass() {
        List<Transaction> transactions = List.of(
                new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("40"), new BigDecimal("1")));
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), transactions);

        assertDoesNotThrow(() -> validator.validate(List.of(genesis, second)));
    }

    @Test
    void emptyChainShouldFail() {
        assertThrows(InvalidChainException.class, () -> validator.validate(new ArrayList<>()));
    }

    @Test
    void shouldDetectBrokenLinkBetweenBlocks() {
        Block second = proofOfWork.mineBlock(1, "not-the-real-previous-hash", List.of());

        assertThrows(InvalidChainException.class, () -> validator.validate(List.of(genesis, second)));
    }

    @Test
    void shouldDetectBlockThatSpendsMoneyThatDoesNotExist() {
        // Correctly mined and linked, but Bob never received any money.
        List<Transaction> transactions = List.of(
                new TransferTransaction("BOB-02", "ALICE-01", new BigDecimal("10"), BigDecimal.ZERO));
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), transactions);

        assertThrows(InvalidChainException.class, () -> validator.validate(List.of(genesis, second)));
    }

    @Test
    void shouldDetectBlockWithWrongIndex() {
        Block second = proofOfWork.mineBlock(5, genesis.getHash(), List.of());

        assertThrows(InvalidChainException.class, () -> validator.validate(List.of(genesis, second)));
    }
}
