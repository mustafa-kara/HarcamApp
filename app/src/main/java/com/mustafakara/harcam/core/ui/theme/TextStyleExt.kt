package com.mustafakara.harcam.core.ui.theme

import androidx.compose.ui.text.TextStyle

/**
 * Apply tabular (monospaced) figures so money/percentage/count digits align and never jump
 * on change — design.md §4.1. Used by the amount* type tokens.
 */
fun TextStyle.withTabularFigures(): TextStyle = copy(fontFeatureSettings = "tnum")
