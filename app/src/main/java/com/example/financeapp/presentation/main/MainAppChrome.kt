package com.example.financeapp.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.presentation.common.components.base.AppTopBar
import com.example.financeapp.presentation.common.components.base.BottomNavigationBar
import com.example.financeapp.presentation.common.components.base.DetailTopBar
import com.example.financeapp.presentation.common.components.base.FinanceActionButton
import com.example.financeapp.presentation.common.components.icons.FinancePlusIcon
import com.example.financeapp.presentation.navigation.AppRoute
import com.example.financeapp.presentation.navigation.isMainRoute
import java.time.LocalDate

@Composable
internal fun MainAppTopBar(
    route: AppRoute,
    selectedDate: LocalDate,
    onAnalyticsClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    AnimatedContent(targetState = route.isMainRoute(), label = "MainAppTopBar") { isMainRoute ->
        if (isMainRoute) {
            AppTopBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                selectedDate = selectedDate,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick
            )
        } else {
            DetailTopBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                title = androidx.compose.ui.res.stringResource(R.string.analytics_title),
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
internal fun MainFloatingActionButton(
    route: AppRoute,
    onClick: () -> Unit
) {
    val sizing = LocalSizing.current
    AnimatedVisibility(
        visible = route != AppRoute.Analytics && route != AppRoute.Planner,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FinanceActionButton(
            onClick = onClick,
            icon = {
                FinancePlusIcon(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(sizing.icon)
                )
            }
        )
    }
}

@Composable
internal fun MainBottomNavigation(
    route: AppRoute,
    onRouteSelected: (AppRoute) -> Unit
) {
    AnimatedVisibility(
        visible = route.isMainRoute(),
        enter = slideInVertically { height -> height } + fadeIn(),
        exit = slideOutVertically { height -> height } + fadeOut()
    ) {
        BottomNavigationBar(selectedRoute = route, onRouteSelected = onRouteSelected)
    }
}
