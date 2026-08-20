package com.sabid.khotianv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sabid.khotianv2.presentation.navigation.NavRoutes
import com.sabid.khotianv2.presentation.ui.*
import com.sabid.khotianv2.presentation.viewmodel.*
import com.sabid.khotianv2.ui.theme.KhotianNewTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhotianNewTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val updateState by mainViewModel.updateState.collectAsState()
                val startRoute by mainViewModel.startRoute.collectAsState()

                LaunchedEffect(Unit) {
                    mainViewModel.checkForUpdates()
                }

                if (updateState is UpdateUiState.UpdateAvailable) {
                    val release = (updateState as UpdateUiState.UpdateAvailable).release
                    AlertDialog(
                        onDismissRequest = { mainViewModel.dismissUpdate() },
                        title = { Text("Update Available") },
                        text = { Text("A new version (${release.versionName}) is available. Would you like to update?") },
                        confirmButton = {
                            Button(onClick = { mainViewModel.downloadAndInstallUpdate(release) }) {
                                Text("Update")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mainViewModel.dismissUpdate() }) {
                                Text("Later")
                            }
                        }
                    )
                }

                if (startRoute == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val backStack = rememberNavBackStack(startRoute!!)
                    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                    val directive = remember(windowAdaptiveInfo) {
                        calculatePaneScaffoldDirective(windowAdaptiveInfo)
                            .copy(horizontalPartitionSpacerSize = 0.dp)
                    }
                    val listDetailStrategy =
                        rememberListDetailSceneStrategy<NavKey>(directive = directive)

                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        sceneStrategy = listDetailStrategy,
                        entryProvider = entryProvider {
                            entry<NavRoutes.Login> {
                                val viewModel: LoginViewModel = hiltViewModel()
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = {
                                        backStack.clear()
                                        backStack.add(NavRoutes.Home)
                                    }
                                )
                            }
                            entry<NavRoutes.Setup> {
                                val viewModel: SetupViewModel = hiltViewModel()
                                SetupScreen(
                                    viewModel = viewModel,
                                    onSetupSuccess = {
                                        backStack.clear()
                                        backStack.add(NavRoutes.Home)
                                    }
                                )
                            }
                            entry<NavRoutes.Home>(
                                metadata = ListDetailSceneStrategy.listPane()
                            ) {
                                val viewModel: HomeViewModel = hiltViewModel()
                                HomeScreen(
                                    viewModel = viewModel,
                                    onAddTransactionClick = {
                                        backStack.add(NavRoutes.TransactionEntry())
                                    },
                                    onTransactionClick = { transaction ->
                                        backStack.add(NavRoutes.TransactionEntry(transactionId = transaction.id))
                                    },
                                    onAccountClick = { accountId ->
                                        backStack.add(NavRoutes.FinancialAccountLedger(accountId))
                                    },
                                    onPartiesClick = {
                                        backStack.add(NavRoutes.Parties)
                                    },
                                    onDashboardClick = {
                                        backStack.add(NavRoutes.Dashboard)
                                    }
                                )
                            }
                            entry<NavRoutes.Parties>(
                                metadata = ListDetailSceneStrategy.listPane()
                            ) {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                PartiesScreen(
                                    viewModel = viewModel,
                                    onPartyClick = { partyId ->
                                        backStack.add(NavRoutes.PartyLedger(partyId))
                                    },
                                    onAddPartyClick = {
                                        backStack.add(NavRoutes.PartyEntry)
                                    },
                                    onBackClick = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.Dashboard>(
                                metadata = ListDetailSceneStrategy.listPane()
                            ) {
                                val viewModel: DashboardViewModel = hiltViewModel()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onFinancialAccountClick = { accountId ->
                                        backStack.add(NavRoutes.FinancialAccountLedger(accountId))
                                    },
                                    onManageAccountsClick = {
                                        backStack.add(NavRoutes.FinancialAccountEntry)
                                    },
                                    onAddPartyClick = {
                                        backStack.add(NavRoutes.PartyEntry)
                                    },
                                    onAddTransactionClick = {
                                        backStack.add(NavRoutes.TransactionEntry())
                                    },
                                    onCrushingEntryClick = {
                                        backStack.add(NavRoutes.CrushingEntry)
                                    },
                                    onUnitManagementClick = {
                                        backStack.add(NavRoutes.UnitEntry)
                                    },
                                    onBackupClick = {
                                        backStack.add(NavRoutes.Backup)
                                    },
                                    onProfitLossClick = {
                                        backStack.add(NavRoutes.ProfitLoss)
                                    },
                                    onStocktakeClick = {
                                        backStack.add(NavRoutes.Stocktake)
                                    },
                                    onBackClick = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.ProfitLoss>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) {
                                val viewModel: ProfitLossViewModel = hiltViewModel()
                                ProfitLossScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.removeLastOrNull() },
                                    onStocktake = { backStack.add(NavRoutes.Stocktake) }
                                )
                            }
                            entry<NavRoutes.Stocktake>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                StocktakeScreen(
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.PartyLedger>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) { route ->
                                val viewModel: LedgerViewModel = hiltViewModel(
                                    key = "party_ledger_${route.partyId}",
                                    creationCallback = { factory: LedgerViewModel.Factory ->
                                        factory.create(route.partyId)
                                    }
                                )
                                PartyLedgerScreen(
                                    viewModel = viewModel,
                                    onBackClick = { backStack.removeLastOrNull() },
                                    onTransactionClick = { transactionId ->
                                        backStack.add(NavRoutes.TransactionEntry(transactionId = transactionId))
                                    }
                                )
                            }
                            entry<NavRoutes.PartyEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: PartyEntryViewModel = hiltViewModel()
                                PartyEntryScreen(
                                    viewModel = viewModel,
                                    onSuccess = { backStack.removeLastOrNull() },
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.TransactionEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) { route ->
                                val viewModel: TransactionEntryViewModel = hiltViewModel(
                                    key = "tx_entry_${route.transactionId ?: 0}_${route.partyId ?: 0}",
                                    creationCallback = { factory: TransactionEntryViewModel.Factory ->
                                        factory.create(route.partyId, route.transactionId)
                                    }
                                )
                                TransactionEntryScreen(
                                    viewModel = viewModel,
                                    onSuccess = { backStack.removeLastOrNull() },
                                    onAddPartyClick = {
                                        backStack.add(NavRoutes.PartyEntry)
                                    },
                                    onAddProductClick = {
                                        backStack.add(NavRoutes.ProductEntry)
                                    },
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.ProductEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: ProductEntryViewModel = hiltViewModel()
                                ProductEntryScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.UnitEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: UnitEntryViewModel = hiltViewModel()
                                UnitEntryScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.CrushingEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: CrushingEntryViewModel = hiltViewModel()
                                CrushingEntryScreen(
                                    viewModel = viewModel,
                                    onBackClick = { backStack.removeLastOrNull() },
                                    onSuccess = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.Backup>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: BackupViewModel = hiltViewModel()
                                BackupScreen(
                                    viewModel = viewModel,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            entry<NavRoutes.FinancialAccountEntry>(
                                metadata = ListDetailSceneStrategy.extraPane()
                            ) {
                                val viewModel: FinancialAccountEntryViewModel = hiltViewModel()
                                FinancialAccountEntryScreen(
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                    viewModel = viewModel
                                )
                            }
                            entry<NavRoutes.FinancialAccountLedger>(
                                metadata = ListDetailSceneStrategy.detailPane()
                            ) { route ->
                                val viewModel: FinancialAccountLedgerViewModel = hiltViewModel(
                                    key = "acc_ledger_${route.accountId}",
                                    creationCallback = { factory: FinancialAccountLedgerViewModel.Factory ->
                                        factory.create(route.accountId)
                                    }
                                )
                                FinancialAccountLedgerScreen(
                                    viewModel = viewModel,
                                    onBackClick = { backStack.removeLastOrNull() },
                                    onTransactionClick = { transactionId ->
                                        backStack.add(NavRoutes.TransactionEntry(transactionId = transactionId))
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
