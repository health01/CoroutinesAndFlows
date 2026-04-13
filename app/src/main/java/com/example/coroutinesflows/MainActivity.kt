package com.example.coroutinesflows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coroutinesflows.ui.navigation.AppNavigation
import com.example.coroutinesflows.ui.theme.CoroutinesAndFlowsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 唯一的 Activity；UI 全部由 Compose 管理。
 * Single Activity — all UI is handled by Jetpack Compose.
 *
 * @AndroidEntryPoint 讓 Hilt 可以注入此 Activity（及其 Fragment / ViewModel）。
 * @AndroidEntryPoint enables Hilt injection for this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutinesAndFlowsTheme {
                AppNavigation()
            }
        }
    }
}
