package com.nali.blockchain.io;

import com.nali.blockchain.exception.DataLoadException;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvDataLoaderTest {

    @Test
    void shouldLoadValidWallets() throws DataLoadException {
        DataLoader loader = new CsvDataLoader("test-data/wallets-valid.csv", "test-data/transactions.csv");

        Map<Wallet, BigDecimal> wallets = loader.loadWallets();

        assertEquals(2, wallets.size());
        assertEquals(new BigDecimal("100"), wallets.get(new Wallet("ALICE-01", "Alice")));
        assertTrue(loader.getWarnings().isEmpty());
    }

    @Test
    void shouldSkipInvalidLinesAndKeepValidOnes() throws DataLoadException {
        DataLoader loader = new CsvDataLoader("test-data/wallets-invalid.csv", "test-data/transactions.csv");

        Map<Wallet, BigDecimal> wallets = loader.loadWallets();

        // Only ALICE-01 is valid: the other lines have a bad number, a negative balance,
        // a missing column or a duplicate address.
        assertEquals(1, wallets.size());
        assertEquals(4, loader.getWarnings().size());
    }

    @Test
    void shouldLoadTransactionsAndSkipInvalidOnes() throws DataLoadException {
        DataLoader loader = new CsvDataLoader("test-data/wallets-valid.csv", "test-data/transactions.csv");

        List<TransferTransaction> transactions = loader.loadTransactions();

        assertEquals(2, transactions.size());
        assertEquals("ALICE-01", transactions.get(0).getSender());
        assertEquals(new BigDecimal("1.5"), transactions.get(0).getFee());
        assertEquals(1, loader.getWarnings().size());
    }

    @Test
    void shouldThrowExceptionWhenFileIsMissing() {
        DataLoader loader = new CsvDataLoader("test-data/does-not-exist.csv", "test-data/transactions.csv");

        assertThrows(DataLoadException.class, loader::loadWallets);
    }

    @Test
    void shouldThrowExceptionWhenNoWalletIsValid() {
        DataLoader loader = new CsvDataLoader("test-data/wallets-header-only.csv", "test-data/transactions.csv");

        assertThrows(DataLoadException.class, loader::loadWallets);
    }
}
