package com.nali.blockchain.service;

import com.nali.blockchain.exception.BlockchainException;
import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidTransactionException;
import com.nali.blockchain.model.Ledger;
import com.nali.blockchain.model.Transaction;
import com.nali.blockchain.model.TransferTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Transactions that are waiting to be put into a block.
 */
public class Mempool {

    private final List<TransferTransaction> pending = new ArrayList<>();
    // Ids of every transaction ever accepted, so the same transaction can't be submitted twice.
    private final Set<String> knownIds = new HashSet<>();

    public void add(TransferTransaction transaction, Ledger ledger) throws BlockchainException {
        transaction.validate();
        if (knownIds.contains(transaction.getId())) {
            throw new InvalidTransactionException("Transaction " + transaction.getId() + " was already submitted");
        }

        // Money already promised in other pending transactions can't be spent again (double spending).
        String sender = transaction.getSender();
        BigDecimal available = ledger.getBalance(sender).subtract(getPendingSpending(sender));
        if (available.compareTo(transaction.getTotalCost()) < 0) {
            throw new InsufficientFundsException(sender, transaction.getTotalCost(), available);
        }

        pending.add(transaction);
        knownIds.add(transaction.getId());
    }

    public BigDecimal getPendingSpending(String address) {
        return pending.stream()
                .filter(transaction -> transaction.getSender().equals(address))
                .map(TransferTransaction::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns up to maxCount transactions, the highest fees first, because miners prefer
     * the transactions that pay them the most. They stay in the mempool until remove() is called,
     * so nothing is lost if the block cannot be added.
     */
    public List<TransferTransaction> selectTransactions(int maxCount) {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive: " + maxCount);
        }
        return pending.stream()
                .sorted(Comparator.comparing(Transaction::getFee).reversed())
                .limit(maxCount)
                .toList();
    }

    /**
     * Removes transactions that were put into a block.
     */
    public void remove(List<TransferTransaction> transactions) {
        pending.removeAll(transactions);
    }

    public List<TransferTransaction> getPendingTransactions() {
        return Collections.unmodifiableList(pending);
    }

    public int size() {
        return pending.size();
    }
}
