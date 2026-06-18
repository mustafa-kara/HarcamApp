package com.mustafakara.harcam.presentation.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.CachedRatesBanner
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.ExchangeRateRow
import com.mustafakara.harcam.core.ui.components.RateChangeDirection
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.Currency
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Exchange rates — the Retrofit / REST showcase (exchange_rates.md). Base selector + last-updated
 * stamp; a LazyColumn of [ExchangeRateRow]; cached-fallback warning banner with Retry. Four states
 * map 1:1 to [RatesStatus]. Pull-to-refresh isn't available on this Material3 BOM, so refresh is a
 * top-bar action (functional equivalent).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExchangeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Exchange rates", style = HarcamTheme.type.headline) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh, enabled = !state.isRefreshing) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).widthIn(min = 20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh rates")
                        }
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val contentWidth = if (maxWidth >= 600.dp) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = contentWidth) {
                    BaseSelector(
                        selected = state.base,
                        onSelect = viewModel::onBaseChange,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    )
                    LastUpdatedStamp(state)

                    when {
                        state.isLoading -> ExchangeSkeleton()
                        state.isError -> EmptyState(
                            icon = Icons.Outlined.CurrencyExchange,
                            title = "Couldn't load exchange rates",
                            description = "You appear to be offline. Check your connection and try again.",
                            actionText = "Retry",
                            onAction = viewModel::onRefresh,
                        )
                        else -> RatesList(state = state, onRetry = viewModel::onRefresh)
                    }
                }
            }
        }
    }
}

@Composable
private fun BaseSelector(
    selected: Currency,
    onSelect: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.pill))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Spacing.xs),
    ) {
        Currency.entries.forEach { currency ->
            val isSelected = currency == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(currency) })
                    .semantics {
                        contentDescription = if (isSelected) "${currency.code}, selected" else currency.code
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currency.code,
                    style = HarcamTheme.type.label,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else HarcamTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun LastUpdatedStamp(state: ExchangeUiState) {
    val updatedMs = state.lastUpdatedEpochMs ?: return
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            Icons.Outlined.Update,
            contentDescription = null,
            tint = HarcamTheme.colors.info,
            modifier = Modifier.height(16.dp),
        )
        Text(
            text = "Updated ${timeFmt.format(updatedMs)}",
            style = HarcamTheme.type.caption,
            color = HarcamTheme.colors.info,
        )
    }
}

@Composable
private fun RatesList(state: ExchangeUiState, onRetry: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxl),
    ) {
        if (state.status is RatesStatus.Cached) {
            item(key = "cached-banner") {
                CachedRatesBanner(
                    message = "Showing cached rates",
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                )
            }
        }
        items(state.rates, key = { it.code }) { rate ->
            ExchangeRateRow(
                code = rate.code,
                name = rate.name,
                formattedRate = rate.formattedRate,
                changePct = rate.changePct,
                direction = rate.direction.toComponent(),
                isStale = rate.isStale,
            )
        }
    }
}

private fun ChangeDirection.toComponent(): RateChangeDirection = when (this) {
    ChangeDirection.UP -> RateChangeDirection.UP
    ChangeDirection.DOWN -> RateChangeDirection.DOWN
    ChangeDirection.FLAT -> RateChangeDirection.FLAT
}

@Composable
private fun ExchangeSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        repeat(2) {
            SkeletonBlock(Modifier.fillMaxWidth().height(56.dp))
        }
    }
}
