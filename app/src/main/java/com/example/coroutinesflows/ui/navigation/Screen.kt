package com.example.coroutinesflows.ui.navigation

/**
 * 定義所有導航路由的密封類別。
 * Sealed class defining all navigation routes.
 */
sealed class Screen(val route: String, val title: String, val emoji: String) {
    data object Home             : Screen("home",         "首頁 Home",                   "🏠")
    data object BasicCoroutine   : Screen("basic",        "基礎 Coroutine",               "🚀")
    data object Cancellation     : Screen("cancellation", "取消 & 例外處理",               "❌")
    data object BasicFlow        : Screen("flow",         "Flow 基礎",                    "🌊")
    data object StateSharedFlow  : Screen("stateflow",    "StateFlow & SharedFlow",       "📡")
    data object RoomFlow         : Screen("room",         "Room + Flow 即時更新",          "🗄️")
    data object NetworkFlow      : Screen("network",      "Retrofit + Flow 網路請求",      "🌐")
    data object ParallelCalls    : Screen("parallel",     "平行 & 順序 API 呼叫",          "⚡")
    data object AdvancedFlow     : Screen("advanced",     "進階 Flow 最佳實踐",            "🔬")
}
