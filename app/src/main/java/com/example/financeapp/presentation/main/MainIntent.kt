package com.example.financeapp.presentation.main

import androidx.compose.runtime.Immutable
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.presentation.common.placeholders.ScreenError
import java.time.LocalDate

@Immutable
sealed interface MainIntent {
    data class DateSelected(val date: LocalDate) : MainIntent
    data class DeleteTransaction(val transactionId: Long) : MainIntent
    data class RestoreDeletedTransaction(val transaction: Transaction) : MainIntent
    data class DeleteFinancialAccount(val accountId: Long) : MainIntent
    data object RetryFailedSyncOperations : MainIntent
    data object DiscardFailedSyncOperations : MainIntent
    data object DataChanged : MainIntent
    data object Retry : MainIntent
}

@Immutable
sealed interface MainEffect {
    data class DeleteFailed(val error: ScreenError) : MainEffect
    data class TransactionDeleted(val transaction: Transaction) : MainEffect
    data class TransactionRestored(val restored: Boolean) : MainEffect
    data class SyncFailed(val count: Int) : MainEffect
}
