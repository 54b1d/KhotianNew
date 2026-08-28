package com.sabid.khotianv2.util

import com.sabid.khotianv2.domain.model.CommaStyle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CurrencyUtilsTest {

    @Test
    fun `formatAmount International style`() {
        val amount = BigDecimal("1234567.89")
        val formatted = CurrencyUtils.formatAmount(amount, CommaStyle.INTERNATIONAL)
        assertEquals("1,234,567.89", formatted)
    }

    @Test
    fun `formatAmount BD style`() {
        val amount = BigDecimal("1234567.89")
        // 12,34,567.89
        val formatted = CurrencyUtils.formatAmount(amount, CommaStyle.BD)
        assertEquals("12,34,567.89", formatted)
    }

    @Test
    fun `formatAmount BD style large number`() {
        val amount = BigDecimal("123456789.00")
        // 12,34,56,789.00
        val formatted = CurrencyUtils.formatAmount(amount, CommaStyle.BD)
        assertEquals("12,34,56,789.00", formatted)
    }

    @Test
    fun `formatAmount BD style small number`() {
        val amount = BigDecimal("123.45")
        val formatted = CurrencyUtils.formatAmount(amount, CommaStyle.BD)
        assertEquals("123.45", formatted)
    }

    @Test
    fun `formatAmount BD style negative number`() {
        val amount = BigDecimal("-1234567.89")
        val formatted = CurrencyUtils.formatAmount(amount, CommaStyle.BD)
        assertEquals("-12,34,567.89", formatted)
    }

    @Test
    fun `formatAmount null handling`() {
        val formatted = CurrencyUtils.formatAmount(null, CommaStyle.BD)
        assertEquals("0.00", formatted)
    }
}
