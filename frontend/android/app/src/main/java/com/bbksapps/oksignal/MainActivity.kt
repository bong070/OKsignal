package com.bbksapps.oksignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.bbksapps.oksignal.navigation.AppNavGraph
import com.bbksapps.oksignal.ui.theme.OKSignalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OKSignalTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                AppNavGraph()
            }
        }
    }
}