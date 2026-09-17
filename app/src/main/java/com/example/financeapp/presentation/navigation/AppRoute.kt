package com.example.financeapp.presentation.navigation

import androidx.compose.runtime.Immutable

@Immutable
sealed class AppRoute(val route: String) {
    data object Expenses : AppRoute("expenses")
    data object Income : AppRoute("income")
    data object Accounts : AppRoute("accounts")
    data object Planner : AppRoute("planner")
    data object Analytics : AppRoute("analytics")
}

fun AppRoute.isMainRoute(): Boolean {
    return bottomNavItems.any { item -> item.route == this }
}
