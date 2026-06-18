package com.mustafakara.harcam.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.CategoryAvatar
import com.mustafakara.harcam.core.ui.components.CategoryIcons
import com.mustafakara.harcam.core.ui.components.PrimaryButton
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.Currency

/**
 * Edit budget form — sets the monthly total and optional per-category limits, saved in one pass
 * (edit_budget.md). Blank category fields mean "no limit". A non-blocking warning appears when the
 * summed category limits exceed the monthly total.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditBudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Edit budget", style = HarcamTheme.type.headline) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                OutlinedTextField(
                    value = state.monthlyText,
                    onValueChange = viewModel::updateMonthly,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly budget") },
                    supportingText = { Text("Leave blank for no monthly limit") },
                    singleLine = true,
                    leadingIcon = { Text(state.currency.symbol, style = HarcamTheme.type.body) },
                    textStyle = HarcamTheme.type.amount,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )

                if (state.exceedsMonthly) {
                    ExceedsMonthlyWarning()
                }

                Text(
                    "Per category",
                    style = HarcamTheme.type.title,
                    color = HarcamTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
                state.categoryLimits.forEach { field ->
                    CategoryLimitRow(
                        field = field,
                        currency = state.currency,
                        onChange = { viewModel.updateCategory(field.category.id, it) },
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
}

@Composable
private fun ExceedsMonthlyWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = HarcamTheme.colors.warning,
            modifier = Modifier
                .size(IconSize.sm)
                .padding(end = Spacing.xs),
        )
        Text(
            text = "Category limits exceed your monthly budget",
            style = HarcamTheme.type.caption,
            color = HarcamTheme.colors.warning,
        )
    }
}

@Composable
private fun CategoryLimitRow(
    field: CategoryLimitField,
    currency: Currency,
    onChange: (String) -> Unit,
) {
    val colors = HarcamTheme.colors
    val catColor = colors.category.byKey(field.category.colorKey)
    val icon = CategoryIcons.forKey(field.category.iconKey)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(
            color = catColor,
            icon = icon,
            contentDescription = null,
            size = 32.dp,
        )
        Text(
            text = field.category.name,
            style = HarcamTheme.type.body,
            color = colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.md),
        )
        OutlinedTextField(
            value = field.text,
            onValueChange = onChange,
            modifier = Modifier
                .width(140.dp)
                .semantics { contentDescription = "${field.category.name} limit" },
            label = { Text("Limit") },
            singleLine = true,
            leadingIcon = { Text(currency.symbol, style = HarcamTheme.type.caption) },
            textStyle = HarcamTheme.type.amountSm,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        )
    }
}
