package com.mustafakara.harcam.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * App theme — light-first "Calm Money" (design.md §1), with a separately-tuned dark variant.
 *
 * Provides the Material 3 [MaterialTheme] (colorScheme + typography) and the finance-specific
 * [AppColors] via [LocalAppColors]. Screens read tokens through [MaterialTheme.colorScheme] and
 * [HarcamTheme.colors] / [HarcamTheme.type] — never raw hex (design.md §1.2).
 *
 * Dynamic color is intentionally NOT used: the brand palette is the identity.
 */
@Composable
fun HarcamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

/** Convenience accessors for the design tokens not on MaterialTheme. */
object HarcamTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val type: AppTextStyles
        @Composable @ReadOnlyComposable get() = AppType
}
