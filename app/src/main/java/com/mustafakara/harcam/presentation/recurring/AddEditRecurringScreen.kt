package com.mustafakara.harcam.presentation.recurring

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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
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
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Add / Edit recurring expense — add_edit_recurring.md. Name, amount, category, cadence selector,
 * next-due date, and a pause switch; delete action in edit mode. Navigates back on save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditRecurringViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit recurring" else "Add recurring",
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
                                Icons.Outlined.Delete,
                                contentDescription = "Delete recurring expense",
                                tint = HarcamTheme.colors.danger,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
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
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameError,
                    supportingText = if (state.nameError) {
                        { Text("Enter a name") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                )

                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount") },
                    leadingIcon = { Text(state.currency.symbol, style = HarcamTheme.type.body) },
                    singleLine = true,
                    isError = state.amountError,
                    supportingText = if (state.amountError) {
                        { Text("Enter an amount greater than 0") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Category", style = HarcamTheme.type.label, color = HarcamTheme.colors.textSecondary)
                CategoryPicker(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelect = viewModel::selectCategory,
                )
                if (state.categoryError) {
                    Text("Pick a category", style = HarcamTheme.type.caption, color = HarcamTheme.colors.danger)
                }

                Text("Repeats", style = HarcamTheme.type.label, color = HarcamTheme.colors.textSecondary)
                CadenceSelector(selected = state.cadence, onSelect = viewModel::selectCadence)

                DateRow(label = "Next due", dateMs = state.nextDueMs, onClick = { showDatePicker = true })

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Paused", style = HarcamTheme.type.body, color = HarcamTheme.colors.textPrimary)
                    Switch(checked = state.isPaused, onCheckedChange = { viewModel.togglePaused() })
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.nextDueMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(viewModel::updateNextDue)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CadenceSelector(selected: RecurrenceCadence, onSelect: (RecurrenceCadence) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Radius.pill))
            .padding(Spacing.xs),
    ) {
        RecurrenceCadence.entries.forEach { cadence ->
            val isSelected = cadence == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        RoundedCornerShape(Radius.pill),
                    )
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(cadence) })
                    .semantics { contentDescription = if (isSelected) "${cadence.label()}, selected" else cadence.label() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    cadence.label(),
                    style = HarcamTheme.type.label,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else HarcamTheme.colors.textSecondary,
                )
            }
        }
    }
}

private fun RecurrenceCadence.label(): String = when (this) {
    RecurrenceCadence.WEEKLY -> "Weekly"
    RecurrenceCadence.MONTHLY -> "Monthly"
    RecurrenceCadence.YEARLY -> "Yearly"
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
            Column(
                modifier = Modifier
                    .heightIn(min = 72.dp)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(Radius.md),
                    )
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(category.id) })
                    .padding(Spacing.sm)
                    .semantics { contentDescription = if (selected) "${category.name}, selected" else category.name },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                CategoryAvatar(
                    color = colors.category.byKey(category.colorKey),
                    icon = CategoryIcons.forKey(category.iconKey),
                    contentDescription = null,
                    size = 40.dp,
                )
                Text(
                    category.name,
                    style = HarcamTheme.type.caption,
                    color = if (selected) MaterialTheme.colorScheme.primary else colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun DateRow(label: String, dateMs: Long, onClick: () -> Unit) {
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
        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = HarcamTheme.colors.textSecondary)
        Text("$label · ${dateFmt.format(dateMs)}", style = HarcamTheme.type.body, color = HarcamTheme.colors.textPrimary)
    }
}
