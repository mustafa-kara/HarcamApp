package com.mustafakara.harcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mustafakara.harcam.presentation.navigation.MainNavigation
import com.mustafakara.harcam.ui.theme.HarcamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarcamTheme {
                MainNavigation()
            }
        }
    }
}