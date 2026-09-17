package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.common.model.toFinanceListItemUiModel

@Composable
fun TransactionsSectionScreen(
    state: TransactionsSectionState,
    totalLabel: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onTransactionDeleteRequest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val items = state.transactions.map { transaction ->
        transaction.toFinanceListItemUiModel(state.categoriesById)
    }
    val normalizedQuery = searchQuery.trim()
    val filteredItems = if (normalizedQuery.isEmpty()) {
        items
    } else {
        items.filter { item ->
            item.title.contains(normalizedQuery, ignoreCase = true) ||
                item.comment.orEmpty().contains(normalizedQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            label = { Text(stringResource(R.string.transaction_search_label)) },
            placeholder = { Text(stringResource(R.string.transaction_search_hint)) }
        )

        RouteScreenContent(
            modifier = Modifier.weight(1f),
            totalLabel = totalLabel,
            total = state.total,
            items = filteredItems,
            emptyMessage = if (normalizedQuery.isEmpty()) {
                emptyMessage
            } else {
                stringResource(R.string.transaction_search_empty)
            },
            isLoading = state.isLoading,
            error = state.error,
            onRetryClick = onRetry,
            onRefresh = onRetry,
            onItemClick = { item ->
                item.id.toLongOrNull()?.let(onTransactionClick)
            },
            onItemDeleteRequest = { item ->
                item.id.toLongOrNull()?.let(onTransactionDeleteRequest)
            }
        )
    }
}
