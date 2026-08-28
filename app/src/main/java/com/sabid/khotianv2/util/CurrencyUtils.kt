package com.sabid.khotianv2.util

import com.sabid.khotianv2.domain.model.CommaStyle
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object CurrencyUtils {
    fun formatAmount(amount: BigDecimal?, style: CommaStyle): String {
        if (amount == null) return "0.00"
        
        return when (style) {
            CommaStyle.INTERNATIONAL -> {
                val symbols = DecimalFormatSymbols(Locale.US)
                val formatter = DecimalFormat("#,##0.00", symbols)
                formatter.format(amount)
            }
            CommaStyle.BD -> {
                formatBDStyle(amount)
            }
        }
    }

    private fun formatBDStyle(amount: BigDecimal): String {
        val isNegative = amount < BigDecimal.ZERO
        val absAmount = amount.abs()
        
        val stringAmount = absAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
        val parts = stringAmount.split(".")
        val integerPart = parts[0]
        val decimalPart = parts[1]

        if (integerPart.length <= 3) {
            return (if (isNegative) "-" else "") + integerPart + "." + decimalPart
        }

        val lastThree = integerPart.substring(integerPart.length - 3)
        var remaining = integerPart.substring(0, integerPart.length - 3)
        
        val result = StringBuilder()
        while (remaining.length > 2) {
            result.insert(0, "," + remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        
        if (remaining.isNotEmpty()) {
            result.insert(0, remaining)
        } else {
            // Remove leading comma if it exists
            if (result.startsWith(",")) {
                result.deleteAt(0)
            }
        }

        val formattedInteger = result.toString() + "," + lastThree
        return (if (isNegative) "-" else "") + formattedInteger + "." + decimalPart
    }
}
