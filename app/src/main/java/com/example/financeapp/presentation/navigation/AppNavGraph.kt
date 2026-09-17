package com.example.financeapp.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    expensesContent: @Composable (Modifier) -> Unit,
    incomeContent: @Composable (Modifier) -> Unit,
    accountsContent: @Composable (Modifier) -> Unit,
    plannerContent: @Composable (Modifier) -> Unit,
    analyticsContent: @Composable (Modifier) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Expenses.route,
        modifier = modifier
    ) {
        composable(AppRoute.Expenses.route, enterTransition = { appEnterTransition() }, exitTransition = { appExitTransition() }, popEnterTransition = { appEnterTransition() }, popExitTransition = { appExitTransition() }) {
            expensesContent(Modifier.fillMaxSize())
        }
        composable(AppRoute.Income.route, enterTransition = { appEnterTransition() }, exitTransition = { appExitTransition() }, popEnterTransition = { appEnterTransition() }, popExitTransition = { appExitTransition() }) {
            incomeContent(Modifier.fillMaxSize())
        }
        composable(AppRoute.Accounts.route, enterTransition = { appEnterTransition() }, exitTransition = { appExitTransition() }, popEnterTransition = { appEnterTransition() }, popExitTransition = { appExitTransition() }) {
            accountsContent(Modifier.fillMaxSize())
        }
        composable(AppRoute.Planner.route, enterTransition = { appEnterTransition() }, exitTransition = { appExitTransition() }, popEnterTransition = { appEnterTransition() }, popExitTransition = { appExitTransition() }) {
            plannerContent(Modifier.fillMaxSize())
        }
        composable(AppRoute.Analytics.route, enterTransition = { appEnterTransition() }, exitTransition = { appExitTransition() }, popEnterTransition = { appEnterTransition() }, popExitTransition = { appExitTransition() }) {
            analyticsContent(Modifier.fillMaxSize())
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appEnterTransition(): EnterTransition {
    val initialRoute = initialState.destination.route.toAppRouteOrNull()
    val targetRoute = targetState.destination.route.toAppRouteOrNull()

    return when {
        initialRoute?.isMainRoute() == true && targetRoute == AppRoute.Analytics -> {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NavAnimationMillis)
            ) + fadeIn(animationSpec = tween(NavAnimationMillis))
        }
        initialRoute == AppRoute.Analytics && targetRoute?.isMainRoute() == true -> {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NavAnimationMillis)
            ) + fadeIn(animationSpec = tween(NavAnimationMillis))
        }
        else -> fadeIn(animationSpec = tween(BottomNavAnimationMillis))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.appExitTransition(): ExitTransition {
    val initialRoute = initialState.destination.route.toAppRouteOrNull()
    val targetRoute = targetState.destination.route.toAppRouteOrNull()

    return when {
        initialRoute?.isMainRoute() == true && targetRoute == AppRoute.Analytics -> {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NavAnimationMillis)
            ) + fadeOut(animationSpec = tween(NavAnimationMillis))
        }
        initialRoute == AppRoute.Analytics && targetRoute?.isMainRoute() == true -> {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NavAnimationMillis)
            ) + fadeOut(animationSpec = tween(NavAnimationMillis))
        }
        else -> fadeOut(animationSpec = tween(BottomNavAnimationMillis))
    }
}

private fun String?.toAppRouteOrNull(): AppRoute? {
    return when (this) {
        AppRoute.Expenses.route -> AppRoute.Expenses
        AppRoute.Income.route -> AppRoute.Income
        AppRoute.Accounts.route -> AppRoute.Accounts
        AppRoute.Planner.route -> AppRoute.Planner
        AppRoute.Analytics.route -> AppRoute.Analytics
        else -> null
    }
}

private const val NavAnimationMillis = 240
private const val BottomNavAnimationMillis = 120
