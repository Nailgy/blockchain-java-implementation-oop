package com.nali.blockchain.io;

import com.nali.blockchain.exception.DataLoadException;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads wallets and transactions from CSV files in the resources folder.
 * The first line of each file is a header and is ignored.
 * An invalid line is skipped and a warning is recorded, so one bad line
 * does not stop the whole application.
 */
public class CsvDataLoader implements DataLoader {

    private static final String SEPARATOR = ",";

    private final String walletsFile;
    private final String transactionsFile;
    private final List<String> warnings = new ArrayList<>();

    public CsvDataLoader(String walletsFile, String transactionsFile) {
        this.walletsFile = walletsFile;
        this.transactionsFile = transactionsFile;
    }

    @Override
    public Map<Wallet, BigDecimal> loadWallets() throws DataLoadException {
        // LinkedHashMap keeps the order of the file.
        Map<Wallet, BigDecimal> wallets = new LinkedHashMap<>();
        List<String> lines = readLines(walletsFile);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                String[] columns = splitLine(line, 3);
                Wallet wallet = new Wallet(columns[0], columns[1]);
                BigDecimal initialBalance = new BigDecimal(columns[2]);
                if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("initial balance must not be negative");
                }
                if (wallets.containsKey(wallet)) {
                    throw new IllegalArgumentException("duplicate address " + wallet.getAddress());
                }
                wallets.put(wallet, initialBalance);
            } catch (NumberFormatException e) {
                addWarning(walletsFile, i + 1, "invalid number");
            } catch (IllegalArgumentException e) {
                addWarning(walletsFile, i + 1, e.getMessage());
            }
        }

        if (wallets.isEmpty()) {
            throw new DataLoadException("No valid wallet found in " + walletsFile);
        }
        return wallets;
    }

    @Override
    public List<TransferTransaction> loadTransactions() throws DataLoadException {
        List<TransferTransaction> transactions = new ArrayList<>();
        List<String> lines = readLines(transactionsFile);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                String[] columns = splitLine(line, 4);
                transactions.add(new TransferTransaction(columns[0], columns[1],
                        new BigDecimal(columns[2]), new BigDecimal(columns[3])));
            } catch (NumberFormatException e) {
                addWarning(transactionsFile, i + 1, "invalid number");
            } catch (IllegalArgumentException e) {
                addWarning(transactionsFile, i + 1, e.getMessage());
            }
        }
        return transactions;
    }

    @Override
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    private List<String> readLines(String fileName) throws DataLoadException {
        // The files are in src/main/resources, so they are read from the classpath.
        InputStream input = getClass().getClassLoader().getResourceAsStream(fileName);
        if (input == null) {
            throw new DataLoadException("Data file not found: " + fileName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new DataLoadException("Could not read data file: " + fileName, e);
        }
    }

    private String[] splitLine(String line, int expectedColumns) {
        String[] columns = line.split(SEPARATOR);
        if (columns.length != expectedColumns) {
            throw new IllegalArgumentException("expected " + expectedColumns
                    + " columns but found " + columns.length);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        return columns;
    }

    private void addWarning(String fileName, int lineNumber, String reason) {
        warnings.add(fileName + ", line " + lineNumber + " skipped: " + reason);
    }
}
