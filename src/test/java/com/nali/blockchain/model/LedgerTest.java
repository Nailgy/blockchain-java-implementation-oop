package com.nali.blockchain.model;

import com.nali.blockchain.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.nali.blockchain.AmountAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LedgerTest {

    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.credit("ALICE-01", new BigDecimal("100"));
    }

    @Test
    void unknownAddressShouldHaveZeroBalance() {
        assertAmount("0", ledger.getBalance("NOBODY"));
    }

    @Test
    void shouldAddMoneyWhenCredited() {
        ledger.credit("ALICE-01", new BigDecimal("25.5"));

        assertAmount("125.5", ledger.getBalance("ALICE-01"));
    }

    @Test
    void shouldRemoveMoneyWhenDebited() throws InsufficientFundsException {
        ledger.debit("ALICE-01", new BigDecimal("40"));

        assertAmount("60", ledger.getBalance("ALICE-01"));
    }

    @Test
    void shouldAllowDebitOfTheExactBalance() throws InsufficientFundsException {
        ledger.debit("ALICE-01", new BigDecimal("100"));

        assertAmount("0", ledger.getBalance("ALICE-01"));
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> ledger.debit("ALICE-01", new BigDecimal("100.01")));

        assertEquals("ALICE-01", exception.getAddress());
        assertAmount("100", exception.getAvailable());
        // The balance must not change after a failed debit.
        assertAmount("100", ledger.getBalance("ALICE-01"));
    }

    @Test
    void copyShouldBeIndependentFromOriginal() throws InsufficientFundsException {
        Ledger copy = ledger.copy();

        copy.debit("ALICE-01", new BigDecimal("30"));
        copy.credit("BOB-02", new BigDecimal("30"));

        assertAmount("70", copy.getBalance("ALICE-01"));
        assertAmount("100", ledger.getBalance("ALICE-01"));
        assertAmount("0", ledger.getBalance("BOB-02"));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> ledger.credit("ALICE-01", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> ledger.debit("ALICE-01", new BigDecimal("-5")));
    }
}
