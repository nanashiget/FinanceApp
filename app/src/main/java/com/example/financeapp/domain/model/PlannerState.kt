package com.example.financeapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlannerState(
    val monthlyBudgetMinorUnits: Long? = null,
    val goals: List<SavingsGoal> = emptyList(),
    val recurringPayments: List<RecurringPayment> = emptyList()
)

@Serializable
data class SavingsGoal(
    val id: Long,
    val title: String,
    val targetMinorUnits: Long,
    val savedMinorUnits: Long = 0
)

@Serializable
data class RecurringPayment(
    val id: Long,
    val title: String,
    val amountMinorUnits: Long,
    val dayOfMonth: Int
)
