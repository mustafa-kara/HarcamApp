package com.mustafakara.harcam.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustafakara.harcam.core.ui.theme.HarcamTheme

/** Splash gate — branches to onboarding / lock / dashboard (splash.md). Brief logo only. */
@Composable
fun SplashScreen(
    onDestination: (SplashDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.destination.collect(onDestination)
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Savings,
            contentDescription = "Harcam",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
    }
}
