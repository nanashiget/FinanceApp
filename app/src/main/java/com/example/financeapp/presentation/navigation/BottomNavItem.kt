package com.example.financeapp.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.R

enum class BottomNavIcon {
    EXPENSES,
    INCOME,
    ACCOUNTS,
    PLANNER
}

@Immutable
data class BottomNavItem(
    val route: AppRoute,
    val icon: BottomNavIcon,
    @StringRes val labelResId: Int
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoute.Expenses,
        icon = BottomNavIcon.EXPENSES,
        labelResId = R.string.nav_expenses
    ),
    BottomNavItem(
        route = AppRoute.Income,
        icon = BottomNavIcon.INCOME,
        labelResId = R.string.nav_income
    ),
    BottomNavItem(
        route = AppRoute.Accounts,
        icon = BottomNavIcon.ACCOUNTS,
        labelResId = R.string.nav_accounts
    ),
    BottomNavItem(
        route = AppRoute.Planner,
        icon = BottomNavIcon.PLANNER,
        labelResId = R.string.nav_planner
    )
)
