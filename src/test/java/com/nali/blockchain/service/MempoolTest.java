package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidTransactionException;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.TransferTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.nali.blockchain.AmountAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MempoolTest {

    private Ledger ledger;
    private Mempool mempool;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.credit("ALICE-01", new BigDecimal("100"));
        mempool = new Mempool();
    }

    private TransferTransaction transfer(String amount, String fee) {
        return new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal(amount), new BigDecimal(fee));
    }

    @Test
    void shouldAcceptValidTransaction() throws BlockchainException {
        mempool.add(transfer("30", "1"), ledger);

        assertEquals(1, mempool.size());
        assertAmount("31", mempool.getPendingSpending("ALICE-01"));
    }

    @Test
    void shouldRejectSameTransactionTwice() throws BlockchainException {
        TransferTransaction transaction = transfer("10", "1");
        mempool.add(transaction, ledger);

        assertThrows(InvalidTransactionException.class, () -> mempool.add(transaction, ledger));
    }

    @Test
    void shouldPreventDoubleSpendingWithPendingTransactions() throws BlockchainException {
        mempool.add(transfer("60", "1"), ledger);

        // Alice has 100, but 61 is already promised, so 50 + 1 is too much.
        assertThrows(InsufficientFundsException.class, () -> mempool.add(transfer("50", "1"), ledger));
        assertEquals(1, mempool.size());
    }

    @Test
    void shouldAcceptTransactionUsingExactlyTheAvailableMoney() throws BlockchainException {
        mempool.add(transfer("99", "1"), ledger);

        assertEquals(1, mempool.size());
    }

    @Test
    void shouldSelectHighestFeesFirst() throws BlockchainException {
        mempool.add(transfer("10", "1"), ledger);
        mempool.add(transfer("10", "5"), ledger);
        mempool.add(transfer("10", "3"), ledger);

        List<TransferTransaction> selected = mempool.selectTransactions(2);

        assertEquals(2, selected.size());
        assertAmount("5", selected.get(0).getFee());
        assertAmount("3", selected.get(1).getFee());
        // Selecting does not remove anything.
        assertEquals(3, mempool.size());
    }

    @Test
    void shouldRemoveOnlyTheGivenTransactions() throws BlockchainException {
        mempool.add(transfer("10", "1"), ledger);
        mempool.add(transfer("10", "5"), ledger);

        mempool.remove(mempool.selectTransactions(1));

        assertEquals(1, mempool.size());
        assertAmount("1", mempool.getPendingTransactions().get(0).getFee());
    }

    @Test
    void selectingFromEmptyMempoolShouldReturnEmptyList() {
        assertTrue(mempool.selectTransactions(3).isEmpty());
    }

    @Test
    void shouldRejectNonPositiveMaxCount() {
        assertThrows(IllegalArgumentException.class, () -> mempool.selectTransactions(0));
    }
}
