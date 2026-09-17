package com.example.financeapp.presentation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.repository.PlannerPreferencesRepository
import com.example.financeapp.domain.model.PlannerState
import com.example.financeapp.domain.model.RecurringPayment
import com.example.financeapp.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val repository: PlannerPreferencesRepository
) : ViewModel() {
    val state: StateFlow<PlannerState> = repository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlannerState()
    )

    fun setMonthlyBudget(amountMinorUnits: Long?) {
        viewModelScope.launch {
            repository.update { it.copy(monthlyBudgetMinorUnits = amountMinorUnits) }
        }
    }

    fun addGoal(title: String, targetMinorUnits: Long) {
        if (title.isBlank() || targetMinorUnits <= 0) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    goals = current.goals + SavingsGoal(
                        id = System.currentTimeMillis(),
                        title = title.trim(),
                        targetMinorUnits = targetMinorUnits
                    )
                )
            }
        }
    }

    fun addToGoal(goalId: Long, amountMinorUnits: Long) {
        if (amountMinorUnits <= 0) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    goals = current.goals.map { goal ->
                        if (goal.id == goalId) {
                            goal.copy(savedMinorUnits = (goal.savedMinorUnits + amountMinorUnits).coerceAtMost(goal.targetMinorUnits))
                        } else goal
                    }
                )
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.update { current -> current.copy(goals = current.goals.filterNot { it.id == goalId }) }
        }
    }

    fun addRecurringPayment(title: String, amountMinorUnits: Long, dayOfMonth: Int) {
        if (title.isBlank() || amountMinorUnits <= 0 || dayOfMonth !in 1..31) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    recurringPayments = current.recurringPayments + RecurringPayment(
                        id = System.currentTimeMillis(),
                        title = title.trim(),
                        amountMinorUnits = amountMinorUnits,
                        dayOfMonth = dayOfMonth
                    )
                )
            }
        }
    }

    fun deleteRecurringPayment(paymentId: Long) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(recurringPayments = current.recurringPayments.filterNot { it.id == paymentId })
            }
        }
    }
}
