package com.nali.blockchain.util;

import java.math.BigDecimal;

/**
 * Shows every amount in the same way, whatever its scale: 54.0 -> "54", 68.50 -> "68.5".
 */
public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
