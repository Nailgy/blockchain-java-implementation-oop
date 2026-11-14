package com.nali.blockchain.io;

import com.nali.blockchain.exception.DataLoadException;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Loads the initial data of the application.
 * Application only knows this interface, so CSV could be replaced by JSON
 * or a database by adding a new class, without changing the rest of the code.
 */
public interface DataLoader {

    /**
     * @return every wallet with its initial balance
     */
    Map<Wallet, BigDecimal> loadWallets() throws DataLoadException;

    List<TransferTransaction> loadTransactions() throws DataLoadException;

    /**
     * @return messages about the lines that were skipped because they were invalid
     */
    List<String> getWarnings();
}
