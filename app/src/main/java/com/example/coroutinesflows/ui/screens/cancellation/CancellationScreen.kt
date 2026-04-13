package com.example.coroutinesflows.ui.screens.cancellation

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
import com.example.coroutinesflows.viewmodel.CancellationViewModel

@Composable
fun CancellationScreen(
    onBack: () -> Unit,
    viewModel: CancellationViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    DemoScaffold(title = "❌ 取消 & 例外處理", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                """
                【Job Cancellation 取消】
                • cancel() 是協作式的：Coroutine 必須到達 suspend point 才會停止
                  cancel() is cooperative: coroutine stops at next suspension point
                • CPU-bound 迴圈需手動檢查 isActive
                  CPU-bound loops must manually check isActive

                【SupervisorJob】
                • 子 Coroutine 失敗不影響兄弟和父
                  Child failure doesn't cancel siblings or parent

                【CoroutineExceptionHandler】
                • 只對 root coroutine 的 launch 有效
                  Only works for root coroutines launched with launch
                • async 的例外在 .await() 時才拋出
                  async exceptions are thrown at .await()

                【CancellationException ⚠️】
                • 永遠要 re-throw！吞掉會破壞 structured concurrency
                  Always re-throw! Swallowing breaks structured concurrency
                """.trimIndent()
            )

            ActionRow(
                primaryLabel = "▶ 啟動長任務",
                onPrimary = { viewModel.startLongRunningJob() },
                secondaryLabel = "⛔ 取消",
                onSecondary = { viewModel.cancelLongRunningJob() }
            )

            ActionRow(
                primaryLabel = "▶ SupervisorJob Demo",
                onPrimary = { viewModel.demoSupervisorJob() },
                secondaryLabel = "清除 Clear",
                onSecondary = { viewModel.clearLogs() }
            )

            ActionRow(
                primaryLabel = "▶ ExceptionHandler",
                onPrimary = { viewModel.demoExceptionHandler() }
            )

            ActionRow(
                primaryLabel = "▶ CancellationException",
                onPrimary = { viewModel.demoCancellationException() }
            )

            LogOutput(logs = logs)

            InterviewCard(
                questions = listOf(
                    "Q: 如何安全取消 Coroutine？\n  A: job.cancel()；但需到達 suspend point 才停止；CPU 密集需手動 isActive 檢查",
                    "Q: SupervisorJob vs Job？\n  A: SupervisorJob：子失敗不影響兄弟；Job：任何子失敗 → 所有子+父都取消",
                    "Q: 為什麼不能吞掉 CancellationException？\n  A: 它是取消協作機制的信號，吞掉會導致 Coroutine 無法正確停止（leaked coroutine）"
                )
            )
        }
    }
}
