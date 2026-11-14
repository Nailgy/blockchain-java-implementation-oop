package com.nali.blockchain;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.DataLoadException;
import com.nali.blockchain.io.CsvDataLoader;
import com.nali.blockchain.io.DataLoader;
import com.nali.blockchain.model.Blockchain;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.service.BlockchainService;
import com.nali.blockchain.service.ChainValidator;
import com.nali.blockchain.service.Mempool;
import com.nali.blockchain.service.MiningStrategy;
import com.nali.blockchain.service.ProofOfWork;
import com.nali.blockchain.ui.ConsoleMenu;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point. It only creates the objects, connects them together and starts the menu.
 */
public class Application {

    private static final String WALLETS_FILE = "data/wallets.csv";
    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private static final int MINING_DIFFICULTY = 4;
    private static final int MAX_TRANSACTIONS_PER_BLOCK = 3;

    public static void main(String[] args) {
        DataLoader loader = new CsvDataLoader(WALLETS_FILE, TRANSACTIONS_FILE);
        MiningStrategy miningStrategy = new ProofOfWork(MINING_DIFFICULTY);
        BlockchainService service = new BlockchainService(miningStrategy, new ChainValidator(miningStrategy),
                new Ledger(), new Blockchain(), new Mempool(), MAX_TRANSACTIONS_PER_BLOCK);

        try {
            service.initialize(loader.loadWallets());
            submitPendingTransactions(service, loader.loadTransactions());
        } catch (DataLoadException e) {
            System.out.println("Could not load the data: " + e.getMessage());
            return;
        } catch (BlockchainException e) {
            System.out.println("Could not create the blockchain: " + e.getMessage());
            return;
        }

        for (String warning : loader.getWarnings()) {
            System.out.println("Warning: " + warning);
        }
        System.out.println("Loaded " + service.getWallets().size() + " wallets and "
                + service.getPendingTransactions().size() + " pending transactions.");

        new ConsoleMenu(service, new Scanner(System.in), System.out).start();
    }

    // A transaction from the file that breaks a rule is rejected, the others are still loaded.
    private static void submitPendingTransactions(BlockchainService service, List<TransferTransaction> transactions) {
        for (TransferTransaction transaction : transactions) {
            try {
                service.submitTransaction(transaction);
            } catch (BlockchainException e) {
                System.out.println("Rejected transaction from file: " + e.getMessage());
            }
        }
    }
}
