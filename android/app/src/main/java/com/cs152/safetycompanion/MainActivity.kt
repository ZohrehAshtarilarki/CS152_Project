package com.cs152.safetycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cs152.safetycompanion.view.AppRoot
import com.cs152.safetycompanion.view.theme.SafetyCompanionTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SafetyCompanionApp).container
        setContent {
            SafetyCompanionTheme {
                AppRoot(container)
            }
        }
    }
}
