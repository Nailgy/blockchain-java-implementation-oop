package com.nali.blockchain.exception;

import com.nali.blockchain.util.MoneyFormatter;

import java.math.BigDecimal;

/**
 * Thrown when a wallet tries to spend more than it has.
 * The numbers are kept as fields so that tests and the UI can read them.
 */
public class InsufficientFundsException extends BlockchainException {

    private final String address;
    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(String address, BigDecimal requested, BigDecimal available) {
        super("Wallet " + address + " has " + MoneyFormatter.format(available) + " available but "
                + MoneyFormatter.format(requested) + " was requested");
        this.address = address;
        this.requested = requested;
        this.available = available;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getRequested() {
        return requested;
    }

    public BigDecimal getAvailable() {
        return available;
    }
}
