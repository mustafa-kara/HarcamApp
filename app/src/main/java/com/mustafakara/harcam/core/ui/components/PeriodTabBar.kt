package com.mustafakara.harcam.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.ReportPeriod

/**
 * Day/Week/Month/Year segmented control — design.md §8.8. Custom pill row (selected segment gets
 * a surface "thumb" + primary text) so it works on the project's Material3 version and matches
 * the calm pill aesthetic.
 */
@Composable
fun PeriodTabBar(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.pill))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Spacing.xs),
    ) {
        ReportPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else androidx.compose.ui.graphics.Color.Transparent,
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(period) },
                    )
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = period.label(),
                    style = HarcamTheme.type.label,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        HarcamTheme.colors.textSecondary
                    },
                )
            }
        }
    }
}

private fun ReportPeriod.label(): String = when (this) {
    ReportPeriod.DAY -> "Day"
    ReportPeriod.WEEK -> "Week"
    ReportPeriod.MONTH -> "Month"
    ReportPeriod.YEAR -> "Year"
}
