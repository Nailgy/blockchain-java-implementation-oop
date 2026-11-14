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
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Builds the transactions of a correct block: the given transfers followed by the allowed reward.
    private List<Transaction> withCorrectReward(Transaction... transfers) {
        List<Transaction> transactions = new ArrayList<>(List.of(transfers));
        transactions.add(new RewardTransaction("MINER-99", ConsensusRules.calculateReward(transactions)));
        return transactions;
    }

    private TransferTransaction aliceSendsToBob(String amount) {
        return new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal(amount), new BigDecimal("1"));
    }

    private void assertInvalid(List<Block> blocks, String expectedMessagePart) {
        InvalidChainException exception = assertThrows(InvalidChainException.class, () -> validator.validate(blocks));
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                () -> "unexpected message: " + exception.getMessage());
    }

    @Test
    void validChainShouldPass() {
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), withCorrectReward(aliceSendsToBob("40")));

        assertDoesNotThrow(() -> validator.validate(List.of(genesis, second)));
    }

    @Test
    void emptyChainShouldFail() {
        assertThrows(InvalidChainException.class, () -> validator.validate(new ArrayList<>()));
    }

    @Test
    void shouldDetectBrokenLinkBetweenBlocks() {
        Block second = proofOfWork.mineBlock(1, "not-the-real-previous-hash", withCorrectReward());

        assertInvalid(List.of(genesis, second), "does not point to the previous block");
    }

    @Test
    void shouldDetectBlockWithWrongIndex() {
        Block second = proofOfWork.mineBlock(5, genesis.getHash(), withCorrectReward());

        assertInvalid(List.of(genesis, second), "has index 5");
    }

    @Test
    void shouldDetectBlockThatSpendsMoneyThatDoesNotExist() {
        // Correctly mined and linked, but Bob never received any money.
        Transaction transfer = new TransferTransaction("BOB-02", "ALICE-01", new BigDecimal("10"), BigDecimal.ZERO);
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), withCorrectReward(transfer));

        assertInvalid(List.of(genesis, second), "invalid transaction");
    }

    @Test
    void shouldDetectTamperedTransactionWithoutReMining() {
        Block original = proofOfWork.mineBlock(1, genesis.getHash(), withCorrectReward(aliceSendsToBob("40")));
        // The attacker changes the amount but keeps the old nonce instead of mining again.
        Block tampered = new Block(original.getIndex(), original.getPreviousHash(), original.getTimestamp(),
                withCorrectReward(aliceSendsToBob("400")), original.getNonce());

        assertThrows(InvalidChainException.class, () -> validator.validate(List.of(genesis, tampered)));
    }

    @Test
    void shouldRejectBlockWithoutReward() {
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), List.of(aliceSendsToBob("40")));

        assertInvalid(List.of(genesis, second), "must end with the miner reward");
    }

    @Test
    void shouldRejectBlockWithTwoRewards() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new RewardTransaction("ATTACKER", new BigDecimal("1000000")));
        transactions.add(new RewardTransaction("MINER-99", ConsensusRules.MINING_REWARD));
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), transactions);

        assertInvalid(List.of(genesis, second), "more than one reward");
    }

    @Test
    void shouldRejectRewardHigherThanAllowed() {
        // Allowed reward is 50 + fee 1 = 51.
        List<Transaction> transactions = new ArrayList<>(List.of(aliceSendsToBob("40")));
        transactions.add(new RewardTransaction("MINER-99", new BigDecimal("52")));
        Block second = proofOfWork.mineBlock(1, genesis.getHash(), transactions);

        assertInvalid(List.of(genesis, second), "allowed reward is 51");
    }

    @Test
    void shouldRejectTransferInGenesisBlock() {
        List<Transaction> transactions = List.of(
                new RewardTransaction("ALICE-01", new BigDecimal("100")),
                aliceSendsToBob("10"));
        Block badGenesis = proofOfWork.mineBlock(0, Block.GENESIS_PREVIOUS_HASH, transactions);

        assertInvalid(List.of(badGenesis), "Genesis block");
    }

    @Test
    void shouldRejectReMinedBlockWithForgedReward() {
        // The forged block is mined again, so its proof-of-work is valid: only the reward rule catches it.
        List<Transaction> transactions = new ArrayList<>(List.of(aliceSendsToBob("40")));
        transactions.add(new RewardTransaction("ATTACKER", new BigDecimal("1000000")));
        Block forged = proofOfWork.mineBlock(1, genesis.getHash(), transactions);

        assertInvalid(List.of(genesis, forged), "allowed reward");
    }
}
