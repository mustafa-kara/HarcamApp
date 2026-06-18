package com.mustafakara.harcam.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.mustafakara.harcam.R

/**
 * Typography — code translation of docs/design.md §4.
 *
 * Display/headers/balances: Plus Jakarta Sans (soft, friendly — Spendee feel).
 * Body/UI/numbers: IBM Plex Sans (trustworthy fintech body) with tabular figures for money.
 *
 * Fonts are loaded via Google Fonts (downloadable). If the provider is unavailable the system
 * falls back to the platform default, which keeps the build and first paint safe.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val DisplayFont = FontFamily(
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Normal),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Medium),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Plus Jakarta Sans"), provider, FontWeight.Bold),
)

private val BodyFont = FontFamily(
    Font(GoogleFont("IBM Plex Sans"), provider, FontWeight.Normal),
    Font(GoogleFont("IBM Plex Sans"), provider, FontWeight.Medium),
    Font(GoogleFont("IBM Plex Sans"), provider, FontWeight.SemiBold),
)

private val lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    family: FontFamily,
    size: Int,
    line: Int,
    weight: FontWeight,
) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    lineHeightStyle = lineHeightStyle,
)

/**
 * Design type-scale tokens — design.md §4.2. These are the authoritative styles screens use
 * (referenced via [AppTextStyles]); the Material 3 [Typography] below maps roles onto a subset
 * so stock M3 components also pick up the brand fonts.
 */
data class AppTextStyles(
    val displayLg: TextStyle,
    val display: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val amount: TextStyle,
    val amountLg: TextStyle,
    val amountSm: TextStyle,
)

val AppType = AppTextStyles(
    displayLg = style(DisplayFont, 34, 40, FontWeight.Bold),
    display = style(DisplayFont, 28, 34, FontWeight.Bold),
    headline = style(DisplayFont, 22, 28, FontWeight.SemiBold),
    title = style(BodyFont, 17, 24, FontWeight.SemiBold),
    body = style(BodyFont, 16, 24, FontWeight.Normal),
    bodyStrong = style(BodyFont, 16, 24, FontWeight.SemiBold),
    label = style(BodyFont, 14, 20, FontWeight.Medium),
    caption = style(BodyFont, 12, 16, FontWeight.Normal),
    // Money styles carry tabular figures so digits never jump (design.md §4.1).
    amount = style(BodyFont, 18, 24, FontWeight.SemiBold).withTabularFigures(),
    amountLg = style(DisplayFont, 28, 32, FontWeight.Bold).withTabularFigures(),
    amountSm = style(BodyFont, 14, 20, FontWeight.Medium).withTabularFigures(),
)

/** Material 3 Typography mapping the brand fonts onto stock roles. */
val AppTypography = Typography(
    displayLarge = AppType.displayLg,
    displayMedium = AppType.display,
    headlineMedium = AppType.headline,
    titleLarge = AppType.title,
    titleMedium = AppType.title,
    bodyLarge = AppType.body,
    bodyMedium = AppType.body,
    labelLarge = AppType.label,
    labelMedium = AppType.label,
    labelSmall = AppType.caption,
)
