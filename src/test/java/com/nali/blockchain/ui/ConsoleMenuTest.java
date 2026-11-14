package com.nali.blockchain.ui;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.model.Wallet;
import com.nali.blockchain.service.BlockchainService;
import com.nali.blockchain.service.ProofOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleMenuTest {

    private BlockchainService service;

    @BeforeEach
    void setUp() throws BlockchainException {
        // Difficulty 0: mining is instant.
        service = new BlockchainService(new ProofOfWork(0), 3);
        Map<Wallet, BigDecimal> initialBalances = new LinkedHashMap<>();
        initialBalances.put(new Wallet("BOB-02", "Bob"), new BigDecimal("50"));
        initialBalances.put(new Wallet("ALICE-01", "Alice"), new BigDecimal("100"));
        initialBalances.put(new Wallet("MINER-99", "Miner"), BigDecimal.ZERO);
        service.initialize(initialBalances);
    }

    // Runs the menu with the given input and returns everything it printed.
    private String run(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleMenu menu = new ConsoleMenu(service, new Scanner(input),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        menu.start();
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void shouldShowWalletsRichestFirst() {
        String output = run("1\n0\n");

        assertTrue(output.contains("Alice (ALICE-01) : 100"));
        assertTrue(output.indexOf("ALICE-01") < output.indexOf("BOB-02"));
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()));
    }

    @Test
    void shouldCreateTransaction() {
        String output = run("2\nALICE-01\nBOB-02\n10\n1\n0\n");

        assertTrue(output.contains("Transaction added"));
        assertEquals(1, service.getPendingTransactions().size());
    }

    @Test
    void shouldMineBlock() {
        String output = run("4\nMINER-99\n0\n");

        assertTrue(output.contains("Mined Block #1"));
        assertEquals(2, service.getBlocks().size());
    }

    @Test
    void shouldReportUnknownOption() {
        String output = run("9\n0\n");

        assertTrue(output.contains("Unknown option: 9"));
    }

    @Test
    void shouldReportInvalidNumberAndContinue() {
        String output = run("2\nALICE-01\nBOB-02\nabc\n1\n0\n");

        assertTrue(output.contains("'abc' is not a valid number"));
        // The menu continued after the error and executed option 1.
        assertTrue(output.contains("Wallets (richest first):"));
        assertTrue(service.getPendingTransactions().isEmpty());
    }

    @Test
    void shouldShowBusinessErrors() {
        String output = run("2\nALICE-01\nNOBODY\n10\n1\n0\n");

        assertTrue(output.contains("Error: Unknown wallet: NOBODY"));
    }

    @Test
    void shouldRejectBlockIndexThatDoesNotExist() {
        String output = run("7\n5\n0\n");

        assertTrue(output.contains("the block index must be between 0 and 0"));
    }

    @Test
    void shouldStopCleanlyWhenInputEndsInTheMiddleOfAQuestion() {
        // No exception: the menu explains the problem and stops.
        String output = run("2\nALICE-01\n");

        assertTrue(output.contains("no more input"));
        assertTrue(output.contains("Goodbye!"));
    }
}
