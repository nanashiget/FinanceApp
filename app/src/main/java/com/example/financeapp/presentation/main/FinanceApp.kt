package com.example.financeapp.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.R
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorEffect
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorIntent
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorMode
import com.example.financeapp.presentation.bottomSheets.accountEditor.AccountEditorViewModel
import com.example.financeapp.presentation.bottomSheets.settings.SettingsListItem
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorEffect
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorIntent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorMode
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorViewModel
import com.example.financeapp.presentation.common.network.NetworkStatusViewModel
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.navigation.AppRoute
import kotlinx.coroutines.launch

@Composable
fun FinanceApp(
    userSettings: UserSettings,
    hasPinCode: Boolean,
    authProtectionState: AuthProtectionState,
    isBiometricAvailable: Boolean,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onBiometricLoginEnabledChange: (Boolean) -> Unit,
    onBiometricAuthenticationRequest: (onAuthenticated: () -> Unit, onFailure: (isFailedAttempt: Boolean) -> Unit) -> Unit,
    onVerifyPinCode: suspend (String) -> Boolean,
    onSetPinCode: suspend (String) -> Unit,
    onClearPinCode: suspend () -> Unit,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    networkStatusViewModel: NetworkStatusViewModel = hiltViewModel(),
    transactionEditorViewModel: TransactionEditorViewModel = hiltViewModel(),
    accountEditorViewModel: AccountEditorViewModel = hiltViewModel()
) {
    val mainState by mainViewModel.state.collectAsState()
    val transactionEditorState by transactionEditorViewModel.state.collectAsState()
    val accountEditorState by accountEditorViewModel.state.collectAsState()
    val isOnline by networkStatusViewModel.isOnline.collectAsState()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var pendingDeleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }
    var failedSyncOperationsCount by remember { mutableStateOf<Int?>(null) }
    var isSettingsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var activeSettingsItemKey by rememberSaveable { mutableStateOf<String?>(null) }
    val activeSettingsItem = SettingsListItem.fromSaveableKey(activeSettingsItemKey)
        ?.takeUnless { it == SettingsListItem.Biometrics && !isBiometricAvailable }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = navBackStackEntry?.destination?.route.toAppRoute()
    val deleteFailedMessage = stringResource(R.string.delete_failed)
    val deleteFailedNoInternetMessage = stringResource(R.string.delete_failed_no_internet)
    val accountDeleteHasTransactionsMessage = stringResource(R.string.account_delete_has_transactions)
    val transactionSavedLocallyMessage = stringResource(R.string.transaction_saved_locally)
    val biometricFailedMessage = stringResource(R.string.settings_biometry_failed)
    val transactionDeletedMessage = stringResource(R.string.transaction_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    val transactionRestoredMessage = stringResource(R.string.transaction_restored)
    val transactionRestoreFailedMessage = stringResource(R.string.transaction_restore_failed)

    LaunchedEffect(mainViewModel, snackbarHostState) {
        mainViewModel.effects.collect { effect ->
            when (effect) {
                is MainEffect.DeleteFailed -> {
                    val message = when (effect.error) {
                        ScreenError.NO_INTERNET -> deleteFailedNoInternetMessage
                        ScreenError.ACCOUNT_HAS_TRANSACTIONS -> accountDeleteHasTransactionsMessage
                        ScreenError.SERVER_ERROR,
                        ScreenError.TIMEOUT,
                        ScreenError.LOAD_FAILED -> deleteFailedMessage
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is MainEffect.TransactionDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = transactionDeletedMessage,
                        actionLabel = undoLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        mainViewModel.onIntent(MainIntent.RestoreDeletedTransaction(effect.transaction))
                    }
                }
                is MainEffect.TransactionRestored -> snackbarHostState.showSnackbar(
                    if (effect.restored) transactionRestoredMessage else transactionRestoreFailedMessage
                )
                is MainEffect.SyncFailed -> failedSyncOperationsCount = effect.count
            }
        }
    }
    LaunchedEffect(transactionEditorViewModel, mainViewModel, snackbarHostState) {
        transactionEditorViewModel.effects.collect { effect ->
            when (effect) {
                is TransactionEditorEffect.Saved -> {
                    mainViewModel.onIntent(MainIntent.DataChanged)
                    if (effect.transactionId < 0) snackbarHostState.showSnackbar(transactionSavedLocallyMessage)
                }
                TransactionEditorEffect.Close -> Unit
            }
        }
    }
    LaunchedEffect(accountEditorViewModel, mainViewModel) {
        accountEditorViewModel.effects.collect { effect ->
            if (effect is AccountEditorEffect.Saved) mainViewModel.onIntent(MainIntent.DataChanged)
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MainAppTopBar(
                route = selectedRoute,
                selectedDate = mainState.selectedDate,
                onAnalyticsClick = { navController.navigateToRoute(AppRoute.Analytics) },
                onBackClick = navController::navigateBackToMain,
                onSettingsClick = { isSettingsSheetVisible = true }
            )
        },
        floatingActionButton = {
            MainFloatingActionButton(route = selectedRoute) {
                when (selectedRoute) {
                    AppRoute.Accounts -> accountEditorViewModel.onIntent(
                        AccountEditorIntent.Open(AccountEditorMode.Create(userSettings.selectedCurrency))
                    )
                    AppRoute.Expenses,
                    AppRoute.Income -> transactionEditorViewModel.onIntent(
                        TransactionEditorIntent.Open(
                            TransactionEditorMode.Create(
                                transactionType = selectedRoute.toTransactionType(),
                                currency = userSettings.selectedCurrency
                            )
                        )
                    )
                    AppRoute.Planner,
                    AppRoute.Analytics -> Unit
                }
            }
        },
        bottomBar = { MainBottomNavigation(selectedRoute, navController::navigateToRoute) }
    ) { innerPadding ->
        MainNavigationContent(
            navController = navController,
            selectedRoute = selectedRoute,
            mainState = mainState,
            isOnline = isOnline,
            plannerCurrency = userSettings.selectedCurrency,
            onRetry = { mainViewModel.onIntent(MainIntent.Retry) },
            onTransactionClick = { id, type ->
                transactionEditorViewModel.onIntent(
                    TransactionEditorIntent.Open(
                        TransactionEditorMode.Edit(id, type, userSettings.selectedCurrency)
                    )
                )
            },
            onTransactionDeleteRequest = { pendingDeleteTarget = DeleteTarget.Transaction(it) },
            onAccountClick = { id ->
                accountEditorViewModel.onIntent(
                    AccountEditorIntent.Open(AccountEditorMode.Edit(id, userSettings.selectedCurrency))
                )
            },
            onAccountDeleteRequest = { pendingDeleteTarget = DeleteTarget.Account(it) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }

    EditorSheetHost(
        transactionState = transactionEditorState,
        accountState = accountEditorState,
        onTransactionIntent = transactionEditorViewModel::onIntent,
        onAccountIntent = accountEditorViewModel::onIntent
    )
    SettingsSheetHost(
        isVisible = isSettingsSheetVisible,
        activeItem = activeSettingsItem,
        mainState = mainState,
        userSettings = userSettings,
        hasPinCode = hasPinCode,
        authProtectionState = authProtectionState,
        isBiometricAvailable = isBiometricAvailable,
        selectedLanguage = selectedLanguage,
        onLanguageSelected = onLanguageSelected,
        onThemeModeSelected = onThemeModeSelected,
        onCurrencySelected = onCurrencySelected,
        onBiometricLoginEnabledChange = onBiometricLoginEnabledChange,
        onBiometricAuthenticationRequest = onBiometricAuthenticationRequest,
        onVerifyPinCode = onVerifyPinCode,
        onSetPinCode = onSetPinCode,
        onClearPinCode = onClearPinCode,
        onCanAttemptPin = onCanAttemptPin,
        onPinFailure = onPinFailure,
        onAuthSuccess = onAuthSuccess,
        onDismissSettings = {
            isSettingsSheetVisible = false
            activeSettingsItemKey = null
        },
        onDismissActiveItem = { activeSettingsItemKey = null },
        onItemSelected = { activeSettingsItemKey = it.saveableKey },
        onBiometricFailure = {
            coroutineScope.launch { snackbarHostState.showSnackbar(biometricFailedMessage) }
        }
    )
    MainDialogHost(
        deleteTarget = pendingDeleteTarget,
        failedSyncOperationsCount = failedSyncOperationsCount,
        onDeleteConfirmed = { target ->
            when (target) {
                is DeleteTarget.Transaction -> mainViewModel.onIntent(MainIntent.DeleteTransaction(target.id))
                is DeleteTarget.Account -> mainViewModel.onIntent(MainIntent.DeleteFinancialAccount(target.id))
            }
            pendingDeleteTarget = null
        },
        onDeleteDismissed = { pendingDeleteTarget = null },
        onRetryFailedSync = {
            mainViewModel.onIntent(MainIntent.RetryFailedSyncOperations)
            failedSyncOperationsCount = null
        },
        onDiscardFailedSync = {
            mainViewModel.onIntent(MainIntent.DiscardFailedSyncOperations)
            failedSyncOperationsCount = null
        },
        onSyncDialogDismissed = { failedSyncOperationsCount = null }
    )
}

private fun AppRoute.toTransactionType(): TransactionType = when (this) {
    AppRoute.Expenses -> TransactionType.EXPENSE
    AppRoute.Income -> TransactionType.INCOME
    AppRoute.Accounts,
    AppRoute.Planner,
    AppRoute.Analytics -> error("Transaction type is only defined for transaction routes")
}
