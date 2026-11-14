package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InvalidChainException;
import com.nali.blockchain.exception.InvalidTransactionException;
import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Blockchain;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.RewardTransaction;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransferTransaction;
import com.nali.blockchain.model.Wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main business operations: create the first block, submit transactions,
 * mine blocks, read balances and validate the chain.
 */
public class BlockchainService {

    private final Map<String, Wallet> wallets = new LinkedHashMap<>();
    private final Ledger ledger = new Ledger();
    private final Blockchain blockchain = new Blockchain();
    private final Mempool mempool = new Mempool();
    private final MiningStrategy miningStrategy;
    private final ChainValidator chainValidator;
    private final int maxTransactionsPerBlock;

    // The mining strategy is given from outside (Dependency Inversion):
    // this class only depends on the MiningStrategy interface.
    public BlockchainService(MiningStrategy miningStrategy, int maxTransactionsPerBlock) {
        if (maxTransactionsPerBlock <= 0) {
            throw new IllegalArgumentException("maxTransactionsPerBlock must be positive: " + maxTransactionsPerBlock);
        }
        this.miningStrategy = miningStrategy;
        this.chainValidator = new ChainValidator(miningStrategy);
        this.maxTransactionsPerBlock = maxTransactionsPerBlock;
    }

    /**
     * Registers the wallets and creates the genesis block,
     * which gives every wallet its initial balance.
     */
    public void initialize(Map<Wallet, BigDecimal> initialBalances) throws BlockchainException {
        if (!blockchain.isEmpty()) {
            throw new IllegalStateException("The blockchain is already initialized");
        }
        List<Transaction> allocations = new ArrayList<>();
        for (Map.Entry<Wallet, BigDecimal> entry : initialBalances.entrySet()) {
            Wallet wallet = entry.getKey();
            wallets.put(wallet.getAddress(), wallet);
            // A wallet that starts with 0 needs no transaction.
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                allocations.add(new RewardTransaction(wallet.getAddress(), entry.getValue()));
            }
        }
        Block genesis = miningStrategy.mineBlock(0, Block.GENESIS_PREVIOUS_HASH, allocations);
        applyAndAdd(genesis);
    }

    public void submitTransaction(TransferTransaction transaction) throws BlockchainException {
        requireKnownWallet(transaction.getSender());
        requireKnownWallet(transaction.getReceiver());
        mempool.add(transaction, ledger);
    }

    /**
     * Puts the pending transactions with the highest fees into a new block.
     * The miner receives the mining reward plus all the fees of the block.
     */
    public Block mineBlock(String minerAddress) throws BlockchainException {
        requireKnownWallet(minerAddress);
        List<TransferTransaction> selected = mempool.takeTransactions(maxTransactionsPerBlock);

        List<Transaction> blockTransactions = new ArrayList<>(selected);
        // The reward must be the last transaction of the block: ChainValidator checks it.
        blockTransactions.add(new RewardTransaction(minerAddress, ConsensusRules.calculateReward(selected)));

        Block block = miningStrategy.mineBlock(blockchain.size(), blockchain.getLatestBlock().getHash(), blockTransactions);
        applyAndAdd(block);
        return block;
    }

    private void applyAndAdd(Block block) throws BlockchainException {
        for (Transaction transaction : block.getTransactions()) {
            // Polymorphism: a transfer moves money, a reward creates money.
            transaction.applyTo(ledger);
        }
        blockchain.addBlock(block);
    }

    public void validateChain() throws InvalidChainException {
        chainValidator.validate(blockchain.getBlocks());
    }

    public void validateChain(List<Block> blocks) throws InvalidChainException {
        chainValidator.validate(blocks);
    }

    /**
     * For the demo: returns a COPY of the chain where someone added 1000 coins
     * to a block without mining it again. The real chain is not changed.
     */
    public List<Block> createTamperedCopy(int blockIndex) {
        if (blockIndex < 0 || blockIndex >= blockchain.size()) {
            throw new IllegalArgumentException("No block with index " + blockIndex);
        }
        Block original = blockchain.getBlocks().get(blockIndex);
        List<Transaction> forgedTransactions = new ArrayList<>(original.getTransactions());
        forgedTransactions.add(new RewardTransaction("ATTACKER", new BigDecimal("1000")));

        Block forged = new Block(original.getIndex(), original.getPreviousHash(),
                original.getTimestamp(), forgedTransactions, original.getNonce());

        List<Block> copy = new ArrayList<>(blockchain.getBlocks());
        copy.set(blockIndex, forged);
        return copy;
    }

    public Optional<Wallet> findWallet(String address) {
        return Optional.ofNullable(wallets.get(address));
    }

    private void requireKnownWallet(String address) throws InvalidTransactionException {
        if (findWallet(address).isEmpty()) {
            throw new InvalidTransactionException("Unknown wallet: " + address);
        }
    }

    public BigDecimal getBalance(String address) {
        return ledger.getBalance(address);
    }

    public Collection<Wallet> getWallets() {
        return wallets.values();
    }

    /**
     * Ranking of the wallets, the richest first.
     */
    public List<Wallet> getWalletsByBalance() {
        return wallets.values().stream()
                .sorted(Comparator.comparing((Wallet wallet) -> getBalance(wallet.getAddress())).reversed())
                .toList();
    }

    public List<Block> getBlocks() {
        return blockchain.getBlocks();
    }

    public List<TransferTransaction> getPendingTransactions() {
        return mempool.getPendingTransactions();
    }
}
