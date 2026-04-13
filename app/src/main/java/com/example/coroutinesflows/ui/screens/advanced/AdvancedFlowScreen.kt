package com.example.coroutinesflows.ui.screens.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coroutinesflows.ui.components.ActionRow
import com.example.coroutinesflows.ui.components.DemoScaffold
import com.example.coroutinesflows.ui.components.InfoCard
import com.example.coroutinesflows.ui.components.InterviewCard
import com.example.coroutinesflows.ui.components.LogOutput
import com.example.coroutinesflows.viewmodel.AdvancedFlowViewModel

@Composable
fun AdvancedFlowScreen(
    onBack: () -> Unit,
    viewModel: AdvancedFlowViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    DemoScaffold(title = "🔬 進階 Flow 最佳實踐", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                """
                【debounce + flatMapLatest = 搜尋防抖黃金組合】
                debounce(300)：停頓 300ms 才繼續（避免每個字母都呼叫 API）
                distinctUntilChanged：相同值不重複觸發
                flatMapLatest：新搜尋開始時取消上一個未完成的 Flow

                【backpressure 處理策略】
                buffer(n)：緩衝 n 個值，生產消費可並發
                conflate()：只保留最新值（適合 UI 刷新率 < 資料更新率）
                collectLatest：每次新值取消正在進行的 collect block

                【shareIn vs stateIn】
                shareIn → SharedFlow（無初始值，Event 用）
                stateIn  → StateFlow（有初始值，State 用）
                """.trimIndent()
            )

            // ── 搜尋防抖 debounce demo ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "🔍 debounce + flatMapLatest 搜尋防抖",
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        label = { Text("輸入搜尋詞 Type to search (debounce 300ms)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    searchResults.forEach { result ->
                        Text(
                            "  • $result",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            ActionRow(
                primaryLabel = "▶ buffer() Demo",
                onPrimary = { viewModel.demoBuffer() },
                secondaryLabel = "清除",
                onSecondary = { viewModel.clearLogs() }
            )

            ActionRow(
                primaryLabel = "▶ conflate() Demo",
                onPrimary = { viewModel.demoConflate() }
            )

            ActionRow(
                primaryLabel = "▶ shareIn Demo",
                onPrimary = { viewModel.demoShareIn() }
            )

            ActionRow(
                primaryLabel = "▶ distinctUntilChanged",
                onPrimary = { viewModel.demoDistinctUntilChanged() }
            )

            LogOutput(logs = logs)

            InterviewCard(
                questions = listOf(
                    "Q: Flow 中 backpressure 如何處理？\n  A: buffer(n)：緩衝並並發；conflate()：跳過中間值只留最新；collectLatest：取消舊 collect",
                    "Q: shareIn 和 stateIn 的差異？\n  A: shareIn → SharedFlow（replay 可設，無初始值）；stateIn → StateFlow（必需初始值，最新值）",
                    "Q: 搜尋防抖的標準 Flow 實作？\n  A: _query.debounce(300).distinctUntilChanged().flatMapLatest { flow { emit(api.search(it)) } }.stateIn(...)"
                )
            )
        }
    }
}
