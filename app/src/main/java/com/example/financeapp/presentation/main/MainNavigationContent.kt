package com.example.financeapp.presentation.main

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.accounts.AccountsRoute
import com.example.financeapp.presentation.analytics.AnalyticsRoute
import com.example.financeapp.presentation.common.network.NetworkStatusBanner
import com.example.financeapp.presentation.common.network.PendingSyncStatusBanner
import com.example.financeapp.presentation.expenses.ExpensesRoute
import com.example.financeapp.presentation.income.IncomeRoute
import com.example.financeapp.presentation.navigation.AppNavGraph
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import com.example.financeapp.presentation.planner.PlannerRoute
import kotlin.math.abs

@Composable
internal fun MainNavigationContent(
    navController: NavHostController,
    selectedRoute: AppRoute,
    mainState: MainState,
    isOnline: Boolean,
    plannerCurrency: Currency,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onTransactionClick: (Long, TransactionType) -> Unit,
    onTransactionDeleteRequest: (Long) -> Unit,
    onAccountClick: (Long) -> Unit,
    onAccountDeleteRequest: (Long) -> Unit
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    val edgeGuardPx = with(density) { spacing.contentSwipeEdgeGuard.toPx() }
    val swipeThresholdPx = with(density) { spacing.contentSwipeThreshold.toPx() }

    Column(modifier = modifier.fillMaxSize()) {
        if (!isOnline) NetworkStatusBanner(modifier = Modifier.fillMaxWidth())
        if (mainState.hasPendingSync) PendingSyncStatusBanner(modifier = Modifier.fillMaxWidth())

        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(edgeGuardPx, swipeThresholdPx, selectedRoute) {
                    var dragOffset = 0f
                    var canHandleSwipe = false
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragOffset = 0f
                            canHandleSwipe = selectedRoute.isMainRoute() &&
                                offset.x > edgeGuardPx && offset.x < size.width - edgeGuardPx
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (canHandleSwipe) {
                                dragOffset += dragAmount
                                change.consume()
                            }
                        },
                        onDragCancel = {
                            dragOffset = 0f
                            canHandleSwipe = false
                        },
                        onDragEnd = {
                            if (canHandleSwipe && abs(dragOffset) >= swipeThresholdPx) {
                                val nextRoute = if (dragOffset < 0) {
                                    selectedRoute.nextMainRoute()
                                } else {
                                    selectedRoute.previousMainRoute()
                                }
                                navController.navigateToRoute(nextRoute)
                            }
                            dragOffset = 0f
                            canHandleSwipe = false
                        }
                    )
                },
            expensesContent = { screenModifier ->
                ExpensesRoute(
                    modifier = screenModifier,
                    state = mainState.expensesState,
                    onRetry = onRetry,
                    onTransactionClick = { id -> onTransactionClick(id, TransactionType.EXPENSE) },
                    onTransactionDeleteRequest = onTransactionDeleteRequest
                )
            },
            incomeContent = { screenModifier ->
                IncomeRoute(
                    modifier = screenModifier,
                    state = mainState.incomeState,
                    onRetry = onRetry,
                    onTransactionClick = { id -> onTransactionClick(id, TransactionType.INCOME) },
                    onTransactionDeleteRequest = onTransactionDeleteRequest
                )
            },
            accountsContent = { screenModifier ->
                AccountsRoute(
                    modifier = screenModifier,
                    state = mainState.accountsState,
                    onRetry = onRetry,
                    onAccountClick = onAccountClick,
                    onAccountDeleteRequest = onAccountDeleteRequest
                )
            },
            plannerContent = { screenModifier ->
                PlannerRoute(currency = plannerCurrency, modifier = screenModifier)
            },
            analyticsContent = { screenModifier ->
                AnalyticsRoute(modifier = screenModifier, onBack = navController::navigateBackToMain)
            }
        )
    }
}
