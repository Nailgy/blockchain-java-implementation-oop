package com.nali.blockchain;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Assertions for money amounts used by all test classes.
 * BigDecimal.equals() also compares the scale (1.0 is not equal to 1.00),
 * so amounts must be compared with compareTo().
 */
public final class AmountAssertions {

    private AmountAssertions() {
    }

    public static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }
}
