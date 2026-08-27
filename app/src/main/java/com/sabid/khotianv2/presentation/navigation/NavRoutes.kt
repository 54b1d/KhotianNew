package com.sabid.khotianv2.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoutes : NavKey {
    @Serializable
    data object Login : NavRoutes

    @Serializable
    data object Setup : NavRoutes

    @Serializable
    data object Dashboard : NavRoutes

    @Serializable
    data object Home : NavRoutes

    @Serializable
    data object Parties : NavRoutes

    @Serializable
    data class PartyLedger(val partyId: Long) : NavRoutes

    @Serializable
    data class PartyEntry(val partyId: Long? = null) : NavRoutes

    @Serializable
    data class TransactionEntry(val partyId: Long? = null, val transactionId: Long? = null) : NavRoutes

    @Serializable
    data object ProductEntry : NavRoutes

    @Serializable
    data object UnitEntry : NavRoutes

    @Serializable
    data object CrushingEntry : NavRoutes

    @Serializable
    data object Backup : NavRoutes

    @Serializable
    data object FinancialAccountEntry : NavRoutes

    @Serializable
    data class FinancialAccountLedger(val accountId: Long) : NavRoutes

    @Serializable
    data object ProfitLoss : NavRoutes

    @Serializable
    data object Stocktake : NavRoutes
    @Serializable
    data object UserManagement : NavRoutes
}
