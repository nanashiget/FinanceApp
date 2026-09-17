package com.example.financeapp.presentation.main

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute

internal fun String?.toAppRoute(): AppRoute = when (this) {
    AppRoute.Income.route -> AppRoute.Income
    AppRoute.Accounts.route -> AppRoute.Accounts
    AppRoute.Planner.route -> AppRoute.Planner
    AppRoute.Analytics.route -> AppRoute.Analytics
    else -> AppRoute.Expenses
}

internal fun NavHostController.navigateBackToMain() {
    if (!popBackStack()) navigateToRoute(AppRoute.Expenses)
}

internal fun NavHostController.navigateToRoute(route: AppRoute) {
    if (currentDestination?.route == route.route) return
    navigate(route.route) {
        if (route.isMainRoute()) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            restoreState = true
        }
        launchSingleTop = true
    }
}

internal fun AppRoute.nextMainRoute(): AppRoute = when (this) {
    AppRoute.Expenses -> AppRoute.Income
    AppRoute.Income -> AppRoute.Accounts
    AppRoute.Accounts -> AppRoute.Planner
    AppRoute.Planner -> AppRoute.Planner
    AppRoute.Analytics -> AppRoute.Expenses
}

internal fun AppRoute.previousMainRoute(): AppRoute = when (this) {
    AppRoute.Expenses -> AppRoute.Expenses
    AppRoute.Income -> AppRoute.Expenses
    AppRoute.Accounts -> AppRoute.Income
    AppRoute.Planner -> AppRoute.Accounts
    AppRoute.Analytics -> AppRoute.Expenses
}
