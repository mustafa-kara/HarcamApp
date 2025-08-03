package com.mustafakara.harcam.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Renk Paleti
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Custom Modern Colors
val ExpenseBlue = Color(0xFF2196F3)
val ExpenseBlueVariant = Color(0xFF1976D2)
val ExpenseGreen = Color(0xFF4CAF50)
val ExpenseGreenVariant = Color(0xFF388E3C)
val ExpenseOrange = Color(0xFFFF9800)
val ExpenseOrangeVariant = Color(0xFFF57C00)
val ExpenseRed = Color(0xFFF44336)
val ExpenseRedVariant = Color(0xFFD32F2F)
val ExpensePurple = Color(0xFF9C27B0)
val ExpensePurpleVariant = Color(0xFF7B1FA2)

// Gradient Colors
val SunsetStart = Color(0xFFFF512F)
val SunsetEnd = Color(0xFFDD2476)

val OceanStart = Color(0xFF2196F3)
val OceanEnd = Color(0xFF21CBF3)

val ForestStart = Color(0xFF11998E)
val ForestEnd = Color(0xFF38EF7D)

val TwilightStart = Color(0xFF667EEA)
val TwilightEnd = Color(0xFFF093FB)

val AutumnStart = Color(0xFFFF8A00)
val AutumnEnd = Color(0xFFE52E71)

// Gradient Brushes
val SunsetGradient = Brush.horizontalGradient(
    listOf(SunsetStart, SunsetEnd)
)

val OceanGradient = Brush.horizontalGradient(
    listOf(OceanStart, OceanEnd)
)

val ForestGradient = Brush.horizontalGradient(
    listOf(ForestStart, ForestEnd)
)

val TwilightGradient = Brush.horizontalGradient(
    listOf(TwilightStart, TwilightEnd)
)

val AutumnGradient = Brush.horizontalGradient(
    listOf(AutumnStart, AutumnEnd)
)

// Expense Category Colors
val ExpenseCategoryColors = listOf(
    ExpenseBlue,
    ExpenseGreen,
    ExpenseOrange,
    ExpenseRed,
    ExpensePurple,
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
    Color(0xFFE91E63), // Pink
    Color(0xFF009688), // Teal
    Color(0xFFFFEB3B)  // Yellow
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

val LightColorScheme = lightColorScheme(
    primary = ExpenseBlue,
    secondary = ExpenseGreen,
    tertiary = ExpenseOrange,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)