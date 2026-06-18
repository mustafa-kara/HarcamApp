package com.mustafakara.harcam.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
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
import com.mustafakara.harcam.domain.model.Currency

/**
 * First-run onboarding (onboarding.md). Currency picker, a read-only preview of the seeded default
 * categories, and an optional app-lock toggle that opens a PIN-create sheet. "Get started" seeds +
 * persists and flips the onboarded flag.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPinSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.done.collect { onDone() } }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text("Welcome to Harcam", style = HarcamTheme.type.display, color = HarcamTheme.colors.textPrimary)
            Text(
                "A calm way to track spending.",
                style = HarcamTheme.type.body,
                color = HarcamTheme.colors.textSecondary,
            )

            Text("Currency", style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
            CurrencyPicker(selected = state.selectedCurrency, onSelect = viewModel::selectCurrency)

            Text("Starter categories", style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                state.defaultCategories.forEach { preview ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CategoryAvatar(
                            color = HarcamTheme.colors.category.byKey(preview.colorKey),
                            icon = CategoryIcons.forKey(preview.iconKey),
                            contentDescription = preview.name,
                            size = 40.dp,
                        )
                        Text(
                            preview.name,
                            style = HarcamTheme.type.caption,
                            color = HarcamTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = Spacing.xs),
                        )
                    }
                }
            }

            LockToggleRow(
                enabled = state.lockEnabled && state.pinConfirmed,
                onToggle = { on ->
                    if (on) showPinSheet = true else viewModel.toggleLock(false)
                },
            )

            if (state.errorMessage != null) {
                Text(state.errorMessage!!, style = HarcamTheme.type.caption, color = HarcamTheme.colors.danger)
            }
        }

        PrimaryButton(
            text = "Get started",
            onClick = viewModel::getStarted,
            loading = state.isSubmitting,
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(Spacing.lg),
        )
    }

    if (showPinSheet) {
        PinCreateSheet(
            onConfirmed = { pin ->
                viewModel.onPinConfirmed(pin)
                showPinSheet = false
            },
            onDismiss = { showPinSheet = false },
        )
    }
}

@Composable
private fun CurrencyPicker(selected: Currency, onSelect: (Currency) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
        Currency.entries.forEach { currency ->
            val isSelected = currency == selected
            val bg = if (isSelected) {
                androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.surface
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .background(bg, RoundedCornerShape(Radius.md))
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(currency) })
                    .padding(vertical = Spacing.md)
                    .semantics { contentDescription = if (isSelected) "${currency.code}, selected" else currency.code },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${currency.symbol} ${currency.code}",
                    style = HarcamTheme.type.label,
                    color = if (isSelected) {
                        androidx.compose.material3.MaterialTheme.colorScheme.primary
                    } else {
                        HarcamTheme.colors.textSecondary
                    },
                )
            }
        }
    }
}

@Composable
private fun LockToggleRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.material3.MaterialTheme.colorScheme.surface,
                RoundedCornerShape(Radius.md),
            )
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("App lock", style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
            Text(
                "PIN required, fingerprint optional.",
                style = HarcamTheme.type.caption,
                color = HarcamTheme.colors.textSecondary,
            )
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

