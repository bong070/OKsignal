package com.bbksapps.oksignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.bbksapps.oksignal.navigation.AppNavGraph
import com.bbksapps.oksignal.ui.theme.OKSignalTheme
import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inviteToken = extractInviteToken(intent)

        setContent {
            OKSignalTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                AppNavGraph(initialInviteToken = inviteToken)
            }
        }
    }

    private fun extractInviteToken(intent: Intent?): String? {
        val data: Uri? = intent?.data
        return data?.getQueryParameter("token")
    }
}