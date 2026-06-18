package com.mustafakara.harcam.presentation.recurring

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.RecurringRow
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import com.mustafakara.harcam.domain.model.RecurringExpense
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil

/**
 * Recurring list — recurring_list.md. Subscriptions/recurring templates sorted by next-due, with a
 * "Due soon" badge inside the reminder window. FAB opens the add form. Four-state via the UiState.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringListScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecurringListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Recurring", style = HarcamTheme.type.headline) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add recurring expense")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> RecurringSkeleton()
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.Autorenew,
                    title = "No recurring expenses yet",
                    description = "Add a subscription or regular bill to track and auto-log it.",
                    actionText = "Add recurring",
                    onAction = onAdd,
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = Spacing.xxxl)) {
                    items(state.items, key = { it.id }) { item ->
                        RecurringRow(
                            name = item.name,
                            category = state.categoriesById[item.categoryId],
                            cadenceLabel = cadenceLabel(item),
                            amount = formatter.format(item.amount, item.currency),
                            dueSoon = isDueSoon(item),
                            isPaused = item.isPaused,
                            onClick = { onEdit(item.id) },
                        )
                    }
                }
            }
        }
    }
}

private val dueFmt = SimpleDateFormat("d MMM", Locale.getDefault())

private fun cadenceLabel(item: RecurringExpense): String {
    val cadence = when (item.cadence) {
        RecurrenceCadence.WEEKLY -> "Weekly"
        RecurrenceCadence.MONTHLY -> "Monthly"
        RecurrenceCadence.YEARLY -> "Yearly"
    }
    return if (item.isPaused) "$cadence · paused" else "$cadence · due ${dueFmt.format(item.nextDueDate)}"
}

private fun isDueSoon(item: RecurringExpense): Boolean {
    if (item.isPaused) return false
    val now = System.currentTimeMillis()
    val days = ceil((item.nextDueDate - now) / 86_400_000.0).toInt()
    return days in 0..maxOf(item.reminderDaysBefore, 3)
}

@Composable
private fun RecurringSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.md),
    ) {
        repeat(4) { SkeletonBlock(Modifier.fillMaxWidth().height(56.dp)) }
    }
}
