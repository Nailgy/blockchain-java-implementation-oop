package com.nali.blockchain.model;

import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidTransactionException;
import com.nali.blockchain.util.MoneyFormatter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Common parent of all transactions.
 * It is an abstract class (and not an interface) because all transactions share
 * the same data (id, receiver, amount, timestamp) and some common code (validate, toDataString).
 * What differs between them is how they change the balances: see applyTo().
 */
public abstract class Transaction {

    private final String id;
    private final String receiver;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;

    protected Transaction(String receiver, BigDecimal amount) {
        this.id = UUID.randomUUID().toString();
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.timestamp = LocalDateTime.now();
    }

    public abstract TransactionType getType();

    public abstract String getSender();

    /**
     * Changes the balances of the ledger. Each type of transaction does it differently.
     */
    public abstract void applyTo(Ledger ledger) throws InsufficientFundsException;

    /**
     * Checks the rules that do not depend on balances.
     * This method is final: a subclass cannot skip the common rules,
     * it can only add its own rules in validateSpecificRules().
     */
    public final void validate() throws InvalidTransactionException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive, but was " + amount);
        }
        validateSpecificRules();
    }

    /**
     * Extra rules of a subclass. By default there are none.
     */
    protected void validateSpecificRules() throws InvalidTransactionException {
    }

    public BigDecimal getFee() {
        return BigDecimal.ZERO;
    }

    /**
     * The text that is used to calculate the hash of a block.
     * The order of the fields must never change, otherwise old hashes would not match anymore.
     */
    public String toDataString() {
        return getType() + ";" + id + ";" + getSender() + ";" + receiver + ";"
                + amount.toPlainString() + ";" + getFee().toPlainString() + ";" + timestamp;
    }

    public String getId() {
        return id;
    }

    public String getReceiver() {
        return receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + getType() + "] " + getSender() + " -> " + receiver
                + " : " + MoneyFormatter.format(amount) + " (fee " + MoneyFormatter.format(getFee()) + ")";
    }
}
