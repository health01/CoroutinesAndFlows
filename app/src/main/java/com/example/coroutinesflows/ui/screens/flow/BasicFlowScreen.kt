package com.example.coroutinesflows.ui.screens.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coroutinesflows.ui.components.ActionRow
import com.example.coroutinesflows.ui.components.DemoScaffold
import com.example.coroutinesflows.ui.components.InfoCard
import com.example.coroutinesflows.ui.components.InterviewCard
import com.example.coroutinesflows.ui.components.LogOutput
import com.example.coroutinesflows.viewmodel.BasicFlowViewModel

@Composable
fun BasicFlowScreen(
    onBack: () -> Unit,
    viewModel: BasicFlowViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    DemoScaffold(title = "🌊 Flow 基礎", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                """
                【Cold Flow 冷流】
                • 每個 collector 有獨立的執行序列
                  Each collector gets its own independent execution
                • 只有被 collect 才開始執行
                  Starts only when collected

                【中間操作符 Intermediate Operators】
                • filter: 過濾 / map: 轉換 / take: 限制數量
                • onStart / onEach / onCompletion: side-effect
                • catch: 攔截例外（不終止 Flow）
                  catch: intercept exceptions (Flow continues)
                • transform: 最靈活，每個值可 emit 0~N 次
                  transform: most flexible, can emit 0..N items per input

                【終端操作符 Terminal Operators】
                • collect: 消費所有 items
                • toList / first / last / count: 一次性取值
                """.trimIndent()
            )

            ActionRow(
                primaryLabel = "▶ Cold Flow Demo",
                onPrimary = { viewModel.demoColdFlow() },
                secondaryLabel = "清除",
                onSecondary = { viewModel.clearLogs() }
            )

            ActionRow(
                primaryLabel = "▶ 中間操作符",
                onPrimary = { viewModel.demoIntermediateOperators() }
            )

            ActionRow(
                primaryLabel = "▶ transform",
                onPrimary = { viewModel.demoTransform() }
            )

            ActionRow(
                primaryLabel = "▶ 終端操作符",
                onPrimary = { viewModel.demoTerminalOperators() }
            )

            LogOutput(logs = logs)

            InterviewCard(
                questions = listOf(
                    "Q: Flow 和 LiveData 的主要差異？\n  A: Flow = Kotlin 原生、不依賴 Android、可測試、豐富 operators；推薦用 collectAsStateWithLifecycle",
                    "Q: Cold Flow 和 Hot Flow 差異？\n  A: Cold = 每個 collector 獨立執行（flow{}）；Hot = 不管有沒有 collector 都執行（StateFlow, SharedFlow）",
                    "Q: .catch 和 try-catch 在 Flow 中的差異？\n  A: .catch 只攔截上游例外，不影響下游；它是 operator 可以繼續 emit 恢復值"
                )
            )
        }
    }
}
