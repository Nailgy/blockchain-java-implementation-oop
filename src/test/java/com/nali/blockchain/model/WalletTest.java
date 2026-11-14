package com.nali.blockchain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    @Test
    void shouldTrimAddressAndOwnerName() {
        Wallet wallet = new Wallet("  ALICE-01 ", " Alice ");

        assertEquals("ALICE-01", wallet.getAddress());
        assertEquals("Alice", wallet.getOwnerName());
    }

    @Test
    void shouldThrowExceptionWhenAddressIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Wallet("   ", "Alice"));
    }

    @Test
    void shouldThrowExceptionWhenOwnerNameIsNull() {
        assertThrows(NullPointerException.class, () -> new Wallet("ALICE-01", null));
    }

    @Test
    void walletsWithSameAddressShouldBeEqual() {
        Wallet first = new Wallet("ALICE-01", "Alice");
        Wallet second = new Wallet("ALICE-01", "Alice Smith");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void walletsWithDifferentAddressShouldNotBeEqual() {
        assertNotEquals(new Wallet("ALICE-01", "Alice"), new Wallet("BOB-02", "Alice"));
    }
}
