package com.nali.blockchain.model;

import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Money sent from one wallet to another.
 * The sender pays the amount plus a fee; the fee goes to the miner of the block.
 */
public class TransferTransaction extends Transaction {

    private final String sender;
    private final BigDecimal fee;

    public TransferTransaction(String sender, String receiver, BigDecimal amount, BigDecimal fee) {
        super(receiver, amount);
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
        this.fee = Objects.requireNonNull(fee, "fee must not be null");
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * What the sender really pays: the amount plus the fee.
     */
    public BigDecimal getTotalCost() {
        return getAmount().add(fee);
    }

    @Override
    public void validate() throws InvalidTransactionException {
        super.validate();
        if (fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Fee must not be negative, but was " + fee);
        }
        if (sender.equals(getReceiver())) {
            throw new InvalidTransactionException("Sender and receiver must be different: " + sender);
        }
    }

    @Override
    public void applyTo(Ledger ledger) throws InsufficientFundsException {
        ledger.debit(sender, getTotalCost());
        ledger.credit(getReceiver(), getAmount());
    }
}
