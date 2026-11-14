package com.kekih.remotescriptclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kekih.remotescriptclient.ui.MainScreen
import com.kekih.remotescriptclient.ui.theme.Ink
import com.kekih.remotescriptclient.ui.theme.VixHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VixHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    MainScreen()
                }
            }
        }
    }
}
