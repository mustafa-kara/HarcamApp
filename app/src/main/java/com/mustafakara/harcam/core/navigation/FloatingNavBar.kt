package com.mustafakara.harcam.core.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.MinTouchTarget
import com.mustafakara.harcam.core.ui.theme.Motion
import com.mustafakara.harcam.core.ui.theme.Radius

/**
 * Floating bottom navigation — a single rounded, elevated bar inset from the screen edges (2025/26
 * "floating nav" pattern) rather than a full-width Material bar with a tinted pill indicator.
 *
 * Selection is expressed only through color + icon weight (filled vs outlined) and a medium label
 * weight — no background pill — so the bar stays calm and reads as one cohesive surface. Each item
 * keeps a ≥48dp touch target and shows its label, so destinations remain discoverable.
 */
@Composable
fun FloatingNavBar(
    destinations: List<TopLevelDestination>,
    isSelected: (TopLevelDestination) -> Boolean,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Lift the floating bar above the gesture/navigation inset, plus a 12dp gap so it visibly floats.
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .padding(bottom = bottomInset + 12.dp),
        shape = RoundedCornerShape(Radius.xl),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { dest ->
                NavBarItem(
                    destination = dest,
                    selected = isSelected(dest),
                    onClick = { onSelect(dest) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            HarcamTheme.colors.textTertiary
        },
        animationSpec = tween(Motion.FAST),
        label = "navItemColor",
    )

    Column(
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = destination.label,
            style = HarcamTheme.type.caption,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
