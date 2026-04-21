package com.bbksapps.oksignal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bbksapps.oksignal.navigation.AppNavGraph
import com.bbksapps.oksignal.ui.theme.OKSignalTheme
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inviteToken = extractInviteToken(intent)

        setContent {
            OKSignalTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(initialInviteToken = inviteToken)
                }
            }
        }
    }

    private fun extractInviteToken(intent: Intent?): String? {
        val data: Uri? = intent?.data
        return data?.getQueryParameter("token")
    }
}