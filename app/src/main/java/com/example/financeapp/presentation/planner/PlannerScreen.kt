package com.example.financeapp.presentation.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.PlannerState
import com.example.financeapp.domain.model.RecurringPayment
import com.example.financeapp.domain.model.SavingsGoal
import com.example.financeapp.presentation.common.utils.formatWithMinorUnits
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun PlannerRoute(
    currency: Currency,
    modifier: Modifier = Modifier,
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    PlannerScreen(
        state = state,
        currency = currency,
        onBudgetSave = viewModel::setMonthlyBudget,
        onGoalAdd = viewModel::addGoal,
        onGoalTopUp = viewModel::addToGoal,
        onGoalDelete = viewModel::deleteGoal,
        onRecurringAdd = viewModel::addRecurringPayment,
        onRecurringDelete = viewModel::deleteRecurringPayment,
        modifier = modifier
    )
}

@Composable
private fun PlannerScreen(
    state: PlannerState,
    currency: Currency,
    onBudgetSave: (Long?) -> Unit,
    onGoalAdd: (String, Long) -> Unit,
    onGoalTopUp: (Long, Long) -> Unit,
    onGoalDelete: (Long) -> Unit,
    onRecurringAdd: (String, Long, Int) -> Unit,
    onRecurringDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Финансовый план",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            BudgetCard(state.monthlyBudgetMinorUnits, currency, onBudgetSave)
        }
        item {
            GoalCreator(onGoalAdd)
        }
        items(state.goals, key = { "goal-${it.id}" }) { goal ->
            GoalCard(goal, currency, onGoalTopUp, onGoalDelete)
        }
        item {
            RecurringCreator(onRecurringAdd)
        }
        items(state.recurringPayments, key = { "recurring-${it.id}" }) { payment ->
            RecurringCard(payment, currency, onRecurringDelete)
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun BudgetCard(current: Long?, currency: Currency, onSave: (Long?) -> Unit) {
    var input by remember(current) { mutableStateOf(current?.let { minorToInput(it) }.orEmpty()) }
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Бюджет на месяц", style = MaterialTheme.typography.titleLarge)
            if (current != null) Text("Лимит: ${money(current, currency)}")
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { parseMinor(input)?.let { onSave(it) } }) { Text("Сохранить") }
                if (current != null) OutlinedButton(onClick = { input = ""; onSave(null) }) { Text("Убрать") }
            }
        }
    }
}

@Composable
private fun GoalCreator(onAdd: (String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Новая цель", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(title, { title = it }, label = { Text("Например: квартира") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                target, { target = it }, label = { Text("Нужно накопить") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                val amount = parseMinor(target) ?: return@Button
                onAdd(title, amount)
                title = ""; target = ""
            }) { Text("Добавить цель") }
        }
    }
}

@Composable
private fun GoalCard(goal: SavingsGoal, currency: Currency, onTopUp: (Long, Long) -> Unit, onDelete: (Long) -> Unit) {
    var topUp by remember(goal.id) { mutableStateOf("") }
    val progress = if (goal.targetMinorUnits <= 0) 0f else (goal.savedMinorUnits.toFloat() / goal.targetMinorUnits).coerceIn(0f, 1f)
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onDelete(goal.id) }) { Text("×") }
            }
            Text("${money(goal.savedMinorUnits, currency)} из ${money(goal.targetMinorUnits, currency)}")
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = topUp,
                    onValueChange = { topUp = it },
                    label = { Text("Пополнить") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    parseMinor(topUp)?.let { onTopUp(goal.id, it); topUp = "" }
                }) { Text("+") }
            }
        }
    }
}

@Composable
private fun RecurringCreator(onAdd: (String, Long, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Регулярный платёж", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(title, { title = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(amount, { amount = it }, label = { Text("Сумма") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(day, { day = it.filter(Char::isDigit).take(2) }, label = { Text("День месяца (1–31)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                val parsedAmount = parseMinor(amount) ?: return@Button
                val parsedDay = day.toIntOrNull() ?: return@Button
                onAdd(title, parsedAmount, parsedDay)
                title = ""; amount = ""; day = ""
            }) { Text("Добавить платёж") }
        }
    }
}

@Composable
private fun RecurringCard(payment: RecurringPayment, currency: Currency, onDelete: (Long) -> Unit) {
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(payment.title, style = MaterialTheme.typography.titleMedium)
                Text("${money(payment.amountMinorUnits, currency)} · ${payment.dayOfMonth}-го числа")
            }
            IconButton(onClick = { onDelete(payment.id) }) { Text("×") }
        }
    }
}

private fun parseMinor(value: String): Long? = runCatching {
    BigDecimal(value.trim().replace(',', '.'))
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
        .takeIf { it > 0 }
}.getOrNull()

private fun minorToInput(value: Long): String = BigDecimal.valueOf(value, 2).stripTrailingZeros().toPlainString()
private fun money(value: Long, currency: Currency): String = Money(value, currency).formatWithMinorUnits()
