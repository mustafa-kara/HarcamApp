package com.mustafakara.harcam.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.CategoryAvatar
import com.mustafakara.harcam.core.ui.components.CategoryIcons
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.PrimaryButton
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.ListRowMinHeight
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.usecase.CategoryWithSpend

private val PaletteKeys = listOf(
    "food", "transport", "bills", "shopping", "health", "entertainment", "other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Categories", style = HarcamTheme.type.headline) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::startAdd) {
                        Icon(Icons.Filled.Add, contentDescription = "Add category")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> CategoryListSkeleton(Modifier.padding(padding))
            state.isEmpty -> EmptyState(
                icon = Icons.Outlined.Category,
                title = "No categories yet",
                description = "Add a category to organize your spending.",
                actionText = "Add category",
                onAction = viewModel::startAdd,
                modifier = Modifier.padding(padding),
            )
            else -> CategoryListContent(
                state = state,
                formatter = formatter,
                onOpenDetail = onOpenDetail,
                modifier = Modifier.padding(padding),
            )
        }
    }

    val editing = state.editing
    if (editing != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissEdit,
            sheetState = sheetState,
        ) {
            CategoryEditor(
                editing = editing,
                onName = viewModel::updateName,
                onColor = viewModel::updateColor,
                onIcon = viewModel::updateIcon,
                onSave = viewModel::saveEdit,
            )
        }
    }

    val deleteTarget = state.deleteTarget
    if (deleteTarget != null) {
        val count = state.items.firstOrNull { it.category.id == deleteTarget.id }
            ?.transactionCount ?: 0
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete ${deleteTarget.name}?", style = HarcamTheme.type.title) },
            text = {
                Text(
                    "Reassign $count expenses to Other and delete?",
                    style = HarcamTheme.type.body,
                    color = HarcamTheme.colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Delete", style = HarcamTheme.type.label, color = HarcamTheme.colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text("Cancel", style = HarcamTheme.type.label)
                }
            },
        )
    }
}

@Composable
private fun CategoryListContent(
    state: CategoryListUiState,
    formatter: MoneyFormatter,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.xxxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        item {
            Text(
                "This month",
                style = HarcamTheme.type.label,
                color = HarcamTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = Spacing.xs),
            )
        }
        items(state.items, key = { it.category.id }) { item ->
            CategoryRow(
                item = item,
                formatter = formatter,
                currency = state.currency,
                onClick = { onOpenDetail(item.category.id) },
            )
        }
    }
}

@Composable
private fun CategoryRow(
    item: CategoryWithSpend,
    formatter: MoneyFormatter,
    currency: com.mustafakara.harcam.domain.model.Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    val category = item.category
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(
            color = colors.category.byKey(category.colorKey),
            icon = CategoryIcons.forKey(category.iconKey),
            contentDescription = null,
            size = 40.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md),
        ) {
            Text(
                category.name,
                style = HarcamTheme.type.body,
                color = colors.textPrimary,
            )
            Text(
                "${item.transactionCount} expenses",
                style = HarcamTheme.type.caption,
                color = colors.textSecondary,
            )
        }
        Text(
            text = formatter.format(item.monthSpent, currency),
            style = HarcamTheme.type.amount,
            color = colors.textPrimary,
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

@Composable
private fun CategoryEditor(
    editing: CategoryEditState,
    onName: (String) -> Unit,
    onColor: (String) -> Unit,
    onIcon: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Text(
            text = if (editing.id == null) "New category" else "Edit category",
            style = HarcamTheme.type.title,
            color = colors.textPrimary,
        )
        OutlinedTextField(
            value = editing.name,
            onValueChange = onName,
            label = { Text("Name") },
            singleLine = true,
            isError = editing.nameError,
            supportingText = if (editing.nameError) {
                { Text("A category with this name already exists") }
            } else {
                null
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("Color", style = HarcamTheme.type.label, color = colors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PaletteKeys.forEach { key ->
                    val swatch = colors.category.byKey(key)
                    val selected = key == editing.colorKey
                    Box(
                        modifier = Modifier
                            .size(MinTouchTarget)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onColor(key) },
                            )
                            .semantics {
                                contentDescription = if (selected) "$key color, selected" else "$key color"
                            }
                            .padding(Spacing.xs)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) colors.outlineStrong else swatch.base,
                                shape = CircleShape,
                            )
                            .padding(if (selected) 3.dp else 0.dp)
                            .background(swatch.base, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.category.byKey(key).container,
                                modifier = Modifier.size(IconSize.md),
                            )
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("Icon", style = HarcamTheme.type.label, color = colors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CategoryIcons.available.forEach { iconKey ->
                    val selected = iconKey == editing.iconKey
                    Box(
                        modifier = Modifier
                            .size(MinTouchTarget)
                            .background(
                                if (selected) {
                                    colors.category.byKey(editing.colorKey).container
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md),
                            )
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onIcon(iconKey) },
                            )
                            .semantics {
                                contentDescription = if (selected) "$iconKey icon, selected" else "$iconKey icon"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            CategoryIcons.forKey(iconKey),
                            contentDescription = null,
                            tint = if (selected) {
                                colors.category.byKey(editing.colorKey).base
                            } else {
                                colors.textSecondary
                            },
                            modifier = Modifier.size(IconSize.lg),
                        )
                    }
                }
            }
        }

        PrimaryButton(
            text = "Save category",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CategoryListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        repeat(6) {
            SkeletonBlock(Modifier.fillMaxWidth().height(56.dp))
        }
    }
}
