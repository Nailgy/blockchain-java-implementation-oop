package com.nali.blockchain.model;

import com.nali.blockchain.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Keeps the current balance of every wallet address.
 * Balances can only change through credit() and debit(), which check the rules,
 * so a balance can never become negative.
 */
public class Ledger {

    private final Map<String, BigDecimal> balances = new HashMap<>();

    public BigDecimal getBalance(String address) {
        return balances.getOrDefault(address, BigDecimal.ZERO);
    }

    public void credit(String address, BigDecimal amount) {
        checkAmount(address, amount);
        balances.put(address, getBalance(address).add(amount));
    }

    public void debit(String address, BigDecimal amount) throws InsufficientFundsException {
        checkAmount(address, amount);
        BigDecimal current = getBalance(address);
        if (current.compareTo(amount) < 0) {
            throw new InsufficientFundsException(address, amount, current);
        }
        balances.put(address, current.subtract(amount));
    }

    public Map<String, BigDecimal> getAllBalances() {
        return Collections.unmodifiableMap(balances);
    }

    private void checkAmount(String address, BigDecimal amount) {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
    }
}
