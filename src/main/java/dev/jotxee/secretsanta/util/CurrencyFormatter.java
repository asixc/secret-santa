package dev.jotxee.secretsanta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    /**
     * Formatea un importe eliminando decimales .0, pero mostrando dos decimales cuando existen.
     * Ej: 50 -> "50", 50.5 -> "50.50".
     */
    public static String formatAmount(Double amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal value = BigDecimal.valueOf(amount);
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.toPlainString();
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
