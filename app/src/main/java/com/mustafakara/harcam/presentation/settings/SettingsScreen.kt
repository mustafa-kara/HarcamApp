package com.mustafakara.harcam.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.ListRowMinHeight
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.repository.PreferencesRepository.ThemeMode
import com.mustafakara.harcam.presentation.auth.PinCreateSheet

/**
 * Settings (settings.md). Appearance (theme), currency, security (lock + biometric + mask),
 * notifications, and navigation shortcuts. Toggles persist immediately (no Save button); disabling
 * the lock asks for confirmation; enabling it captures a PIN via the shared create sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenExchange: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPinSheet by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings", style = HarcamTheme.type.headline) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("APPEARANCE")
            ThemeSelector(selected = state.themeMode, onSelect = viewModel::setThemeMode)

            SettingsRow(
                title = "Currency",
                value = "${state.currency.symbol} ${state.currency.code}",
                onClick = { showCurrencySheet = true },
            )

            SectionHeader("SECURITY")
            ToggleRow(
                title = "App lock",
                subtitle = "Require a PIN to open the app",
                checked = state.lockEnabled,
                onCheckedChange = { on -> if (on) showPinSheet = true else showDisableConfirm = true },
            )
            ToggleRow(
                title = "Biometric unlock",
                subtitle = "Use fingerprint or face when available",
                checked = state.biometricEnabled,
                enabled = state.lockEnabled,
                onCheckedChange = viewModel::setBiometric,
            )
            ToggleRow(
                title = "Mask amounts",
                subtitle = "Hide balances until tapped",
                checked = state.maskAmounts,
                onCheckedChange = viewModel::setMaskAmounts,
            )

            SectionHeader("NOTIFICATIONS")
            ToggleRow(
                title = "Budget alerts",
                subtitle = "Notify at 80% and over budget",
                checked = state.budgetAlerts,
                onCheckedChange = viewModel::setBudgetAlerts,
            )
            ToggleRow(
                title = "Recurring reminders",
                subtitle = "Remind before recurring expenses are due",
                checked = state.recurringReminders,
                onCheckedChange = viewModel::setRecurringReminders,
            )

            SectionHeader("MANAGE")
            SettingsRow(title = "Categories", onClick = onOpenCategories)
            SettingsRow(title = "Recurring", onClick = onOpenRecurring)
            SettingsRow(title = "Exchange rates", onClick = onOpenExchange)

            SectionHeader("ABOUT")
            Text(
                "Harcam · Version 1.0.0",
                style = HarcamTheme.type.caption,
                color = HarcamTheme.colors.textTertiary,
                modifier = Modifier.padding(Spacing.lg),
            )
        }
    }

    if (showPinSheet) {
        PinCreateSheet(
            onConfirmed = { pin ->
                viewModel.enableLock(pin)
                showPinSheet = false
            },
            onDismiss = { showPinSheet = false },
        )
    }

    if (showDisableConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDisableConfirm = false },
            title = { Text("Disable app lock?") },
            text = { Text("Anyone with your device will be able to open Harcam.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.disableLock()
                    showDisableConfirm = false
                }) { Text("Disable", color = HarcamTheme.colors.danger) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDisableConfirm = false }) {
                    Text("Keep")
                }
            },
        )
    }

    if (showCurrencySheet) {
        CurrencySheet(
            selected = state.currency,
            onSelect = {
                viewModel.setCurrency(it)
                showCurrencySheet = false
            },
            onDismiss = { showCurrencySheet = false },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = HarcamTheme.type.label,
        color = HarcamTheme.colors.textSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    )
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Radius.pill))
            .padding(Spacing.xs),
    ) {
        ThemeMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        RoundedCornerShape(Radius.pill),
                    )
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(mode) })
                    .semantics { contentDescription = if (isSelected) "${mode.label()}, selected" else mode.label() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    mode.label(),
                    style = HarcamTheme.type.label,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else HarcamTheme.colors.textSecondary,
                )
            }
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = HarcamTheme.type.body, color = HarcamTheme.colors.textPrimary)
            Text(subtitle, style = HarcamTheme.type.caption, color = HarcamTheme.colors.textSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingsRow(title: String, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = HarcamTheme.type.body, color = HarcamTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = HarcamTheme.type.bodyStrong, color = HarcamTheme.colors.textSecondary)
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = HarcamTheme.colors.textTertiary,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySheet(selected: Currency, onSelect: (Currency) -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Text("Currency", style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
            Currency.entries.forEach { currency ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MinTouchTarget)
                        .selectable(
                            selected = currency == selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(currency) },
                        )
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${currency.symbol}  ${currency.code}",
                        style = HarcamTheme.type.body,
                        color = if (currency == selected) MaterialTheme.colorScheme.primary else HarcamTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}
