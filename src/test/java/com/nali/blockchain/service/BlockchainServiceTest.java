package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.exception.InvalidTransactionException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nali.blockchain.AmountAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainServiceTest {

    private BlockchainService service;

    @BeforeEach
    void setUp() throws BlockchainException {
        // Difficulty 1 keeps the tests fast.
        service = new BlockchainService(new ProofOfWork(1), 2);

        Map<Wallet, BigDecimal> initialBalances = new LinkedHashMap<>();
        initialBalances.put(new Wallet("ALICE-01", "Alice"), new BigDecimal("100"));
        initialBalances.put(new Wallet("BOB-02", "Bob"), new BigDecimal("50"));
        initialBalances.put(new Wallet("MINER-99", "Miner"), BigDecimal.ZERO);
        service.initialize(initialBalances);
    }

    @Test
    void genesisBlockShouldGiveInitialBalances() {
        assertEquals(1, service.getBlocks().size());
        assertAmount("100", service.getBalance("ALICE-01"));
        assertAmount("50", service.getBalance("BOB-02"));
        assertAmount("0", service.getBalance("MINER-99"));
    }

    @Test
    void miningShouldApplyTransfersAndRewardTheMiner() throws BlockchainException {
        service.submitTransaction(new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("30"), new BigDecimal("2")));

        Block block = service.mineBlock("MINER-99");

        assertEquals(1, block.getIndex());
        assertAmount("68", service.getBalance("ALICE-01"));
        assertAmount("80", service.getBalance("BOB-02"));
        // Reward 50 + fee 2
        assertAmount("52", service.getBalance("MINER-99"));
        assertTrue(service.getPendingTransactions().isEmpty());
    }

    @Test
    void blockShouldContainAtMostTheMaximumNumberOfTransfers() throws BlockchainException {
        for (int i = 0; i < 3; i++) {
            service.submitTransaction(new TransferTransaction("ALICE-01", "BOB-02", BigDecimal.ONE, BigDecimal.ONE));
        }

        Block block = service.mineBlock("MINER-99");

        // 2 transfers (the maximum) + 1 reward
        assertEquals(3, block.getTransactions().size());
        assertEquals(1, service.getPendingTransactions().size());
    }

    @Test
    void miningWithoutPendingTransactionsShouldOnlyRewardTheMiner() throws BlockchainException {
        service.mineBlock("MINER-99");

        assertAmount("50", service.getBalance("MINER-99"));
    }

    @Test
    void shouldRejectTransactionToUnknownWallet() {
        TransferTransaction transaction = new TransferTransaction("ALICE-01", "NOBODY", BigDecimal.TEN, BigDecimal.ONE);

        assertThrows(InvalidTransactionException.class, () -> service.submitTransaction(transaction));
    }

    @Test
    void shouldRejectTransactionWhenBalanceIsInsufficient() {
        TransferTransaction transaction = new TransferTransaction("BOB-02", "ALICE-01", new BigDecimal("50"), BigDecimal.ONE);

        assertThrows(InsufficientFundsException.class, () -> service.submitTransaction(transaction));
    }

    @Test
    void shouldRejectMiningByUnknownWallet() {
        assertThrows(InvalidTransactionException.class, () -> service.mineBlock("NOBODY"));
    }

    @Test
    void chainShouldBeValidAfterMining() throws BlockchainException {
        service.submitTransaction(new TransferTransaction("ALICE-01", "BOB-02", BigDecimal.TEN, BigDecimal.ONE));
        service.mineBlock("MINER-99");

        assertDoesNotThrow(() -> service.validateChain());
    }

    @Test
    void shouldDetectTamperedBlock() throws BlockchainException {
        service.mineBlock("MINER-99");

        // Block 0 is changed, so block 1 no longer points to it.
        List<Block> tamperedCopy = service.createTamperedCopy(0);

        assertThrows(InvalidChainException.class, () -> service.validateChain(tamperedCopy));
        // The real chain is still valid.
        assertDoesNotThrow(() -> service.validateChain());
    }

    @Test
    void walletsShouldBeRankedByBalance() {
        List<Wallet> ranking = service.getWalletsByBalance();

        assertEquals("ALICE-01", ranking.get(0).getAddress());
        assertEquals("MINER-99", ranking.get(2).getAddress());
    }

    @Test
    void shouldFindOnlyKnownWallets() {
        assertTrue(service.findWallet("BOB-02").isPresent());
        assertTrue(service.findWallet("NOBODY").isEmpty());
    }
}
