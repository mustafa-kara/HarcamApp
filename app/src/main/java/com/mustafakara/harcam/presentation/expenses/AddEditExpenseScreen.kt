package com.mustafakara.harcam.presentation.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.CategoryAvatar
import com.mustafakara.harcam.core.ui.components.CategoryIcons
import com.mustafakara.harcam.core.ui.components.PrimaryButton
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Add / Edit expense form — add_edit_expense.md. Back + title (Add/Edit) top bar with a delete
 * action in edit mode; a big amount display, amount field, category picker, date row + dialog,
 * note field, "Make recurring" switch, and a sticky Save button. Navigates back on save via the
 * [AddEditExpenseUiState.saved] flag.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditExpenseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit expense" else "Add expense",
                        style = HarcamTheme.type.title,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete expense",
                                tint = HarcamTheme.colors.danger,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                AmountDisplay(
                    amount = state.amount,
                    currency = state.currency,
                    formatter = formatter,
                )

                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = state.amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.amountError) {
                    FieldError("Enter an amount greater than 0")
                }

                Text("Category", style = HarcamTheme.type.label, color = HarcamTheme.colors.textSecondary)
                CategoryPicker(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelect = viewModel::selectCategory,
                )
                if (state.categoryError) {
                    FieldError("Pick a category")
                }

                DateRow(
                    dateMs = state.dateMs,
                    onClick = { showDatePicker = true },
                )

                OutlinedTextField(
                    value = state.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Make recurring",
                        style = HarcamTheme.type.body,
                        color = HarcamTheme.colors.textPrimary,
                    )
                    Switch(
                        checked = state.makeRecurring,
                        onCheckedChange = { viewModel.toggleRecurring() },
                    )
                }
            }

            PrimaryButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .imePadding()
                    .padding(Spacing.lg),
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(viewModel::updateDate)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AmountDisplay(
    amount: Double,
    currency: Currency,
    formatter: MoneyFormatter,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatter.formatSigned(amount, currency, isExpense = true),
            style = HarcamTheme.type.amountLg,
            color = HarcamTheme.colors.expense,
        )
    }
}

@Composable
private fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long) -> Unit,
) {
    val colors = HarcamTheme.colors
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.xs),
    ) {
        items(categories, key = { it.id }) { category ->
            val selected = category.id == selectedCategoryId
            val catColor = colors.category.byKey(category.colorKey)
            Column(
                modifier = Modifier
                    .heightIn(min = 72.dp)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(Radius.md),
                    )
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(category.id) },
                    )
                    .padding(Spacing.sm)
                    .semantics {
                        contentDescription = if (selected) "${category.name}, selected" else category.name
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                CategoryAvatar(
                    color = catColor,
                    icon = CategoryIcons.forKey(category.iconKey),
                    contentDescription = null,
                    size = 40.dp,
                )
                Text(
                    text = category.name,
                    style = HarcamTheme.type.caption,
                    color = if (selected) MaterialTheme.colorScheme.primary else colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun DateRow(dateMs: Long, onClick: () -> Unit) {
    val dateFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = HarcamTheme.colors.textSecondary,
        )
        Text(
            text = dateFmt.format(dateMs),
            style = HarcamTheme.type.body,
            color = HarcamTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun FieldError(message: String) {
    Text(
        text = message,
        style = HarcamTheme.type.caption,
        color = HarcamTheme.colors.danger,
    )
}
