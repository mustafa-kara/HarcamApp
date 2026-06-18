package com.mustafakara.harcam.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens — code translation of docs/design.md §2 (Color System).
 *
 * Material 3 [androidx.compose.material3.ColorScheme] covers background/surface/primary etc.;
 * the finance-specific semantic tokens (expense, income, warning, category palette, …) that M3
 * does not model live in [AppColors], exposed via [LocalAppColors].
 *
 * Composables must reference these tokens — never raw Color(0xFF…) — per design.md §1.2.
 */

// ── Raw token values (light) — design.md §2.1 ─────────────────────────────────
private val LightBackground = Color(0xFFF7F8FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFEEF1F5)
private val LightOutline = Color(0xFFE2E6EC)
private val LightOutlineStrong = Color(0xFFCBD2DC)
private val LightTextPrimary = Color(0xFF11151B)
private val LightTextSecondary = Color(0xFF5A6573)
private val LightTextTertiary = Color(0xFF8A929E)
private val LightPrimary = Color(0xFF3B5BDB)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightExpense = Color(0xFFE8654E)
private val LightIncome = Color(0xFF1E9E6A)
private val LightSuccess = Color(0xFF1E9E6A)
private val LightWarning = Color(0xFFC77A12)
private val LightDanger = Color(0xFFD23B3B)
private val LightInfo = Color(0xFF2C7BB0)

// ── Raw token values (dark) ───────────────────────────────────────────────────
private val DarkBackground = Color(0xFF0F1115)
private val DarkSurface = Color(0xFF171A1F)
private val DarkSurfaceVariant = Color(0xFF1F242B)
private val DarkOutline = Color(0xFF2A3038)
private val DarkOutlineStrong = Color(0xFF3A424D)
private val DarkTextPrimary = Color(0xFFF1F3F6)
private val DarkTextSecondary = Color(0xFFA4ADBA)
private val DarkTextTertiary = Color(0xFF6E7682)
private val DarkPrimary = Color(0xFF7E97F5)
private val DarkOnPrimary = Color(0xFF0F1115)
private val DarkExpense = Color(0xFFF3937F)
private val DarkIncome = Color(0xFF4FCB95)
private val DarkSuccess = Color(0xFF4FCB95)
private val DarkWarning = Color(0xFFE2A33F)
private val DarkDanger = Color(0xFFEC6A6A)
private val DarkInfo = Color(0xFF5BA8D8)

/**
 * Category palette — design.md §2.1 (soft, Spendee-inspired). A category's [base] is its
 * accent; [container] is the 12%-opacity tint used behind the category avatar/chip.
 */
@Immutable
data class CategoryColor(val base: Color, val container: Color)

@Immutable
data class CategoryPalette(
    val food: CategoryColor,
    val transport: CategoryColor,
    val bills: CategoryColor,
    val shopping: CategoryColor,
    val health: CategoryColor,
    val entertainment: CategoryColor,
    val other: CategoryColor,
) {
    /** Resolve a palette slot by its stable key (stored on a category). */
    fun byKey(key: String): CategoryColor = when (key) {
        "food" -> food
        "transport" -> transport
        "bills" -> bills
        "shopping" -> shopping
        "health" -> health
        "entertainment" -> entertainment
        else -> other
    }
}

private fun cat(base: Color) = CategoryColor(base = base, container = base.copy(alpha = 0.12f))

private val LightCategoryPalette = CategoryPalette(
    food = cat(Color(0xFFE8654E)),
    transport = cat(Color(0xFF2C7BB0)),
    bills = cat(Color(0xFF7048C4)),
    shopping = cat(Color(0xFFC84F8C)),
    health = cat(Color(0xFF1E9E6A)),
    entertainment = cat(Color(0xFFC77A12)),
    other = cat(Color(0xFF5A6573)),
)

private val DarkCategoryPalette = CategoryPalette(
    food = cat(Color(0xFFF3937F)),
    transport = cat(Color(0xFF5BA8D8)),
    bills = cat(Color(0xFFA488E8)),
    shopping = cat(Color(0xFFE58AB7)),
    health = cat(Color(0xFF4FCB95)),
    entertainment = cat(Color(0xFFE2A33F)),
    other = cat(Color(0xFFA4ADBA)),
)

/** Finance-specific semantic tokens not covered by Material 3 ColorScheme. */
@Immutable
data class AppColors(
    val expense: Color,
    val income: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val outlineStrong: Color,
    val category: CategoryPalette,
    val isDark: Boolean,
) {
    /** 10–16% opacity status backgrounds (design.md §2.1: successBg/warningBg/dangerBg). */
    val successBg: Color get() = success.copy(alpha = if (isDark) 0.14f else 0.10f)
    val warningBg: Color get() = warning.copy(alpha = if (isDark) 0.14f else 0.10f)
    val dangerBg: Color get() = danger.copy(alpha = if (isDark) 0.14f else 0.10f)
}

val LightAppColors = AppColors(
    expense = LightExpense,
    income = LightIncome,
    success = LightSuccess,
    warning = LightWarning,
    danger = LightDanger,
    info = LightInfo,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    outlineStrong = LightOutlineStrong,
    category = LightCategoryPalette,
    isDark = false,
)

val DarkAppColors = AppColors(
    expense = DarkExpense,
    income = DarkIncome,
    success = DarkSuccess,
    warning = DarkWarning,
    danger = DarkDanger,
    info = DarkInfo,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    outlineStrong = DarkOutlineStrong,
    category = DarkCategoryPalette,
    isDark = true,
)

val LocalAppColors = compositionLocalOf { LightAppColors }

// ── Material 3 color schemes mapped from the same tokens ──────────────────────
val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimary.copy(alpha = 0.10f),
    onPrimaryContainer = LightPrimary,
    secondary = LightPrimary,
    onSecondary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = LightDanger,
    onError = LightOnPrimary,
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.copy(alpha = 0.16f),
    onPrimaryContainer = DarkPrimary,
    secondary = DarkPrimary,
    onSecondary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkDanger,
    onError = DarkOnPrimary,
)
