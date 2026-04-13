package com.example.coroutinesflows.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coroutinesflows.ui.screens.advanced.AdvancedFlowScreen
import com.example.coroutinesflows.ui.screens.basic.BasicCoroutineScreen
import com.example.coroutinesflows.ui.screens.cancellation.CancellationScreen
import com.example.coroutinesflows.ui.screens.flow.BasicFlowScreen
import com.example.coroutinesflows.ui.screens.home.HomeScreen
import com.example.coroutinesflows.ui.screens.network.NetworkFlowScreen
import com.example.coroutinesflows.ui.screens.parallel.ParallelCallsScreen
import com.example.coroutinesflows.ui.screens.room.RoomFlowScreen
import com.example.coroutinesflows.ui.screens.stateflow.StateSharedFlowScreen

/**
 * 應用程式頂層導航圖。
 * Top-level navigation graph for the app.
 *
 * 使用 rememberNavController 持有導航狀態；NavHost 根據 route 切換 Composable。
 * Uses rememberNavController to hold nav state; NavHost switches Composables by route.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { navController.navigate(it.route) })
        }
        composable(Screen.BasicCoroutine.route) {
            BasicCoroutineScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Cancellation.route) {
            CancellationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.BasicFlow.route) {
            BasicFlowScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.StateSharedFlow.route) {
            StateSharedFlowScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.RoomFlow.route) {
            RoomFlowScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NetworkFlow.route) {
            NetworkFlowScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ParallelCalls.route) {
            ParallelCallsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdvancedFlow.route) {
            AdvancedFlowScreen(onBack = { navController.popBackStack() })
        }
    }
}
