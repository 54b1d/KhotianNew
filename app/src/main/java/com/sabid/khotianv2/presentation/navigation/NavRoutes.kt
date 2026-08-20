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
    data class PartyLedger(val partyId: Long) : NavRoutes

    @Serializable
    data object PartyEntry : NavRoutes

    @Serializable
    data class TransactionEntry(val partyId: Long? = null) : NavRoutes

    @Serializable
    data object ProductEntry : NavRoutes

    @Serializable
    data object CrushingEntry : NavRoutes
}
