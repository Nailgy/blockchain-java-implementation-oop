package com.nali.blockchain.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyFormatterTest {

    @Test
    void shouldShowSameAmountTheSameWayWhateverTheScale() {
        assertEquals("54", MoneyFormatter.format(new BigDecimal("54.0")));
        assertEquals("54", MoneyFormatter.format(new BigDecimal("54")));
        assertEquals("68.5", MoneyFormatter.format(new BigDecimal("68.50")));
    }

    @Test
    void shouldNotUseScientificNotation() {
        // stripTrailingZeros() alone would turn 100 into 1E+2.
        assertEquals("100", MoneyFormatter.format(new BigDecimal("100")));
    }
}
