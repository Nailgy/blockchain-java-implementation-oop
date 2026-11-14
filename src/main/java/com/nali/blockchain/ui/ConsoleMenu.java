package com.nali.blockchain.ui;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;
import com.nali.blockchain.service.BlockchainService;
import com.nali.blockchain.util.MoneyFormatter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface. It only reads the user input, calls the service
 * and prints the results: there is no business logic here.
 */
public class ConsoleMenu {

    private final BlockchainService service;
    private final Scanner scanner;

    public ConsoleMenu(BlockchainService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
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
                    default -> System.out.println("Unknown option: " + choice);
                }
            } catch (BlockchainException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                // Must be before IllegalArgumentException, because it is a subclass of it.
                System.out.println("Invalid input: please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== Blockchain Ledger =====");
        System.out.println("1. Show wallets and balances");
        System.out.println("2. Create a transaction");
        System.out.println("3. Show pending transactions");
        System.out.println("4. Mine a block");
        System.out.println("5. Show the chain");
        System.out.println("6. Validate the chain");
        System.out.println("7. Tampering demo");
        System.out.println("0. Exit");
        System.out.print("Your choice: ");
    }

    private void showWallets() {
        System.out.println("Wallets (richest first):");
        for (Wallet wallet : service.getWalletsByBalance()) {
            System.out.println("  " + wallet + " : " + MoneyFormatter.format(service.getBalance(wallet.getAddress())));
        }
    }

    private void createTransaction() throws BlockchainException {
        String sender = ask("Sender address: ");
        String receiver = ask("Receiver address: ");
        BigDecimal amount = new BigDecimal(ask("Amount: "));
        BigDecimal fee = new BigDecimal(ask("Fee: "));

        service.submitTransaction(new TransferTransaction(sender, receiver, amount, fee));
        System.out.println("Transaction added to the pending transactions.");
    }

    private void showPendingTransactions() {
        List<TransferTransaction> pending = service.getPendingTransactions();
        if (pending.isEmpty()) {
            System.out.println("No pending transactions.");
            return;
        }
        pending.forEach(transaction -> System.out.println("  " + transaction));
    }

    private void mineBlock() throws BlockchainException {
        String miner = ask("Miner address: ");
        System.out.println("Mining...");
        Block block = service.mineBlock(miner);
        System.out.println("Mined " + block);
    }

    private void showChain() {
        for (Block block : service.getBlocks()) {
            System.out.println(block);
            for (Transaction transaction : block.getTransactions()) {
                System.out.println("    " + transaction);
            }
        }
    }

    private void validateChain() {
        try {
            service.validateChain();
            System.out.println("The chain is VALID.");
        } catch (InvalidChainException e) {
            System.out.println("The chain is INVALID: " + e.getMessage());
        }
    }

    private void tamperingDemo() {
        int index = Integer.parseInt(ask("Index of the block to tamper with: "));
        List<Block> tamperedCopy = service.createTamperedCopy(index);
        System.out.println("A copy of the chain was made where 1000 coins were added to block #" + index + ".");
        try {
            service.validateChain(tamperedCopy);
            System.out.println("Tampering was NOT detected.");
        } catch (InvalidChainException e) {
            System.out.println("Tampering detected: " + e.getMessage());
        }
        System.out.println("(The real chain was not changed.)");
    }

    private String ask(String question) {
        System.out.print(question);
        return scanner.nextLine().trim();
    }
}
