package com.mustafakara.harcam.presentation.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.ScreenHeader
import com.mustafakara.harcam.core.ui.components.ExpenseRow
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.core.util.MoneyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Expense list screen — expense_list.md. AppTopBar + add FAB, a category-filter chip strip, and a
 * day-grouped ledger with swipe-to-delete + Undo. Mirrors DashboardScreen's four-state when-block.
 * Local Room reads never error, so no error state is rendered (see ViewModel KDoc).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.recentlyDeleted) {
        state.recentlyDeleted ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Expense deleted",
            actionLabel = "Undo",
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScreenHeader(title = "Expenses")
            CategoryFilterStrip(
                categories = state.allCategories,
                selectedCategoryId = state.selectedCategoryId,
                onSelect = viewModel::selectCategory,
            )
            when {
                state.isLoading -> ExpenseListSkeleton()
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No spending logged yet",
                    description = "Add your first expense to see it here.",
                    actionText = "Add expense",
                    onAction = onAddExpense,
                )
                else -> ExpenseListContent(
                    state = state,
                    formatter = formatter,
                    onEditExpense = onEditExpense,
                    onDelete = viewModel::deleteExpense,
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterStrip(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            CategoryChip(
                label = "All",
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                selected = selectedCategoryId == category.id,
                onClick = { onSelect(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = HarcamTheme.colors
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else colors.textSecondary
    Box(
        modifier = Modifier
            .height(40.dp)
            .background(background, RoundedCornerShape(Radius.pill))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.pill))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = Spacing.md)
            .semantics { contentDescription = if (selected) "$label, selected" else label },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = HarcamTheme.type.label, color = contentColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseListContent(
    state: ExpenseListUiState,
    formatter: MoneyFormatter,
    onEditExpense: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxxl),
    ) {
        state.days.forEach { day ->
            item(key = "header-${day.dateLabel}") {
                DayHeader(
                    dateLabel = day.dateLabel,
                    dayTotal = formatter.formatSigned(day.dayTotal, state.currency, isExpense = true),
                    dayTotalSpoken = formatter.semanticLabel(day.dayTotal, state.currency, isExpense = true),
                )
            }
            items(day.items, key = { it.id }) { expense ->
                val dismissState = rememberDismissState(
                    confirmValueChange = { value ->
                        if (value == DismissValue.DismissedToStart) {
                            onDelete(expense.id)
                            true
                        } else {
                            false
                        }
                    },
                )
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = { SwipeDeleteBackground() },
                    dismissContent = {
                        ExpenseRow(
                            expense = expense,
                            category = state.categoriesById[expense.categoryId],
                            timeLabel = timeFmt.format(expense.createdAt),
                            formatter = formatter,
                            onClick = { onEditExpense(expense.id) },
                            modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DayHeader(dateLabel: String, dayTotal: String, dayTotalSpoken: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dateLabel, style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
        Text(
            text = dayTotal,
            style = HarcamTheme.type.amount,
            color = HarcamTheme.colors.textSecondary,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = dayTotalSpoken
            },
        )
    }
}

@Composable
private fun SwipeDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HarcamTheme.colors.dangerBg)
            .padding(horizontal = Spacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete",
            tint = HarcamTheme.colors.danger,
        )
    }
}

@Composable
private fun ExpenseListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth(0.4f).height(20.dp))
        repeat(6) {
            SkeletonBlock(Modifier.fillMaxWidth().height(56.dp))
        }
    }
}
