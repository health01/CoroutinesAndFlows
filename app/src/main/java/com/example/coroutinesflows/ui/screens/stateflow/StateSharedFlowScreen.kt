package com.example.coroutinesflows.ui.screens.stateflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coroutinesflows.ui.components.DemoScaffold
import com.example.coroutinesflows.ui.components.InfoCard
import com.example.coroutinesflows.ui.components.InterviewCard
import com.example.coroutinesflows.viewmodel.StateSharedFlowViewModel
import com.example.coroutinesflows.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StateSharedFlowScreen(
    onBack: () -> Unit,
    viewModel: StateSharedFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * 用 LaunchedEffect 消費一次性 SharedFlow 事件。
     * Use LaunchedEffect to consume one-shot SharedFlow events.
     *
     * collectLatest：如果前一個事件還在處理，新事件來時取消前者。
     * 適合 Snackbar：確保快速連續點擊時只顯示最新訊息。
     */
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.Navigate -> {
                    snackbarHostState.showSnackbar("Navigate to: ${event.route}")
                }
                is UiEvent.HideKeyboard -> { /* handle keyboard */ }
            }
        }
    }

    DemoScaffold(title = "📡 StateFlow & SharedFlow", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Snackbar placement
            SnackbarHost(hostState = snackbarHostState)

            InfoCard(
                """
                StateFlow = UI State（有初始值，新 collector 立即收到當前值）
                SharedFlow = UI Event（無初始值，一次性動作如導航、Snackbar）

                黃金法則 Golden Rule:
                • 狀態（State）用 StateFlow：畫面永遠有東西可顯示
                  Use StateFlow for state: screen always has something to show
                • 事件（Event）用 SharedFlow(replay=0)：避免重複消費
                  Use SharedFlow(replay=0) for events: prevent replay on re-subscribe

                UI 消費事件：LaunchedEffect + collectLatest
                Consume events in UI: LaunchedEffect + collectLatest
                """.trimIndent()
            )

            // ── StateFlow Counter ─────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("StateFlow Counter", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    if (uiState.isLoading) {
                        Text("載入中… Loading…")
                    } else {
                        Text(
                            text = "${uiState.count}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.decrement() }) { Text("－") }
                        OutlinedButton(onClick = { viewModel.reset() }) { Text("Reset") }
                        Button(onClick = { viewModel.increment() }) { Text("＋") }
                    }
                }
            }

            // ── SharedFlow Event Triggers ─────────────────────���───────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SharedFlow Events", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.loadDataWithDelay() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("▶ 模擬非同步載入 (async load → Snackbar event)") }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.triggerNavigation() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("▶ 觸發導航事件 (navigation event)") }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.broadcastMessage("Hello from SharedFlow broadcast!") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("▶ Broadcast Snackbar") }
                }
            }

            InterviewCard(
                questions = listOf(
                    "Q: StateFlow 和 SharedFlow 各適合什麼？\n  A: StateFlow → UI State（有值）；SharedFlow(replay=0) → 一次性 Event（導航、Snackbar）",
                    "Q: 為什麼要用 collectAsStateWithLifecycle 而非 collectAsState？\n  A: collectAsStateWithLifecycle 在 App 進背景時自動停止收集，節省資源；推薦用於 Compose",
                    "Q: 如何防止事件在配置變更後重複觸發？\n  A: SharedFlow(replay=0)：新 collector 不會收到舊事件；不要用 replay=1 來發送 Event"
                )
            )
        }
    }
}
