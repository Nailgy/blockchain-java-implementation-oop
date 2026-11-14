package com.nali.blockchain.ui;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;
import com.nali.blockchain.service.BlockchainService;
import com.nali.blockchain.util.MoneyFormatter;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface. It only reads the user input, calls the service
 * and prints the results: there is no business logic here.
 * The input (Scanner) and the output (PrintStream) are given from outside, so the menu can be tested.
 */
public class ConsoleMenu {

    private final BlockchainService service;
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleMenu(BlockchainService service, Scanner scanner, PrintStream out) {
        this.service = service;
        this.scanner = scanner;
        this.out = out;
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            if (!scanner.hasNextLine()) {
                break;
            }
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> showWallets();
                    case "2" -> createTransaction();
                    case "3" -> showPendingTransactions();
                    case "4" -> mineBlock();
                    case "5" -> showChain();
                    case "6" -> validateChain();
                    case "7" -> tamperingDemo();
                    case "0" -> running = false;
                    default -> out.println("Unknown option: " + choice);
                }
            } catch (BlockchainException e) {
                out.println("Error: " + e.getMessage());
            } catch (InvalidInputException e) {
                out.println("Invalid input: " + e.getMessage());
            }
        }
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("===== Blockchain Ledger =====");
        out.println("1. Show wallets and balances");
        out.println("2. Create a transaction");
        out.println("3. Show pending transactions");
        out.println("4. Mine a block");
        out.println("5. Show the chain");
        out.println("6. Validate the chain");
        out.println("7. Tampering demo");
        out.println("0. Exit");
        out.print("Your choice: ");
    }

    private void showWallets() {
        out.println("Wallets (richest first):");
        for (Wallet wallet : service.getWalletsByBalance()) {
            out.println("  " + wallet + " : " + MoneyFormatter.format(service.getBalance(wallet.getAddress())));
        }
    }

    private void createTransaction() throws BlockchainException, InvalidInputException {
        String sender = ask("Sender address: ");
        String receiver = ask("Receiver address: ");
        BigDecimal amount = askAmount("Amount: ");
        BigDecimal fee = askAmount("Fee: ");

        service.submitTransaction(new TransferTransaction(sender, receiver, amount, fee));
        out.println("Transaction added to the pending transactions.");
    }

    private void showPendingTransactions() {
        List<TransferTransaction> pending = service.getPendingTransactions();
        if (pending.isEmpty()) {
            out.println("No pending transactions.");
            return;
        }
        pending.forEach(transaction -> out.println("  " + transaction));
    }

    private void mineBlock() throws BlockchainException, InvalidInputException {
        String miner = ask("Miner address: ");
        out.println("Mining...");
        Block block = service.mineBlock(miner);
        out.println("Mined " + block);
    }

    private void showChain() {
        for (Block block : service.getBlocks()) {
            out.println(block);
            for (Transaction transaction : block.getTransactions()) {
                out.println("    " + transaction);
            }
        }
    }

    private void validateChain() {
        try {
            service.validateChain();
            out.println("The chain is VALID.");
        } catch (InvalidChainException e) {
            out.println("The chain is INVALID: " + e.getMessage());
        }
    }

    private void tamperingDemo() throws InvalidInputException {
        int index = askBlockIndex("Index of the block to tamper with: ");
        List<Block> tamperedCopy = service.createTamperedCopy(index);
        out.println("A copy of the chain was made where 1000 coins were added to block #" + index + ".");
        try {
            service.validateChain(tamperedCopy);
            out.println("Tampering was NOT detected.");
        } catch (InvalidChainException e) {
            out.println("Tampering detected: " + e.getMessage());
        }
        out.println("(The real chain was not changed.)");
    }

    private String ask(String question) throws InvalidInputException {
        out.print(question);
        // Without this check, nextLine() would crash the program when the input ends (Ctrl+D, end of a file).
        if (!scanner.hasNextLine()) {
            throw new InvalidInputException("no more input");
        }
        return scanner.nextLine().trim();
    }

    private BigDecimal askAmount(String question) throws InvalidInputException {
        String text = ask(question);
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + text + "' is not a valid number");
        }
    }

    private int askBlockIndex(String question) throws InvalidInputException {
        String text = ask(question);
        int index;
        try {
            index = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + text + "' is not a whole number");
        }
        int blockCount = service.getBlocks().size();
        if (index < 0 || index >= blockCount) {
            throw new InvalidInputException("the block index must be between 0 and " + (blockCount - 1));
        }
        return index;
    }
}
