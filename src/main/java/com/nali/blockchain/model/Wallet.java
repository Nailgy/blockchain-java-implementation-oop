package com.nali.blockchain.model;

import java.util.Objects;

/**
 * A participant of the blockchain: an address and the name of its owner.
 * The balance is NOT stored here: it is calculated by the Ledger from the blocks of the chain.
 */
public final class Wallet {

    private final String address;
    private final String ownerName;

    public Wallet(String address, String ownerName) {
        this.address = requireNotBlank(address, "address");
        this.ownerName = requireNotBlank(ownerName, "ownerName");
    }

    private static String requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public String getAddress() {
        return address;
    }

    public String getOwnerName() {
        return ownerName;
    }

    // Two wallets are the same wallet if they have the same address.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Wallet other)) {
            return false;
        }
        return address.equals(other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return ownerName + " (" + address + ")";
    }
}
