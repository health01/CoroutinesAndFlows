package com.example.coroutinesflows.ui.screens.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.example.coroutinesflows.viewmodel.BasicCoroutineViewModel

@Composable
fun BasicCoroutineScreen(
    onBack: () -> Unit,
    viewModel: BasicCoroutineViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    DemoScaffold(title = "🚀 基礎 Coroutine", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                """
                【概念 Concept】
                • launch：fire-and-forget，回傳 Job，不能取值
                  launch: fire-and-forget, returns Job (no result)
                • async：回傳 Deferred<T>，需呼叫 .await() 取得結果
                  async: returns Deferred<T>, call .await() for result
                • withContext：切換 Dispatcher 並等待完成（同步語義）
                  withContext: switches Dispatcher and suspends until done
                • Structured Concurrency：子 Coroutine 的生命週期受父 Scope 約束
                  Structured Concurrency: child lifetime bounded by parent scope
                """.trimIndent()
            )

            ActionRow(
                primaryLabel = "▶ launch Demo",
                onPrimary = { viewModel.demoLaunch() },
                secondaryLabel = "清除 Clear",
                onSecondary = { viewModel.clearLogs() }
            )

            ActionRow(
                primaryLabel = "▶ async/await Demo",
                onPrimary = { viewModel.demoAsyncAwait() }
            )

            ActionRow(
                primaryLabel = "▶ withContext Demo",
                onPrimary = { viewModel.demoWithContext() }
            )

            ActionRow(
                primaryLabel = "▶ Structured Concurrency",
                onPrimary = { viewModel.demoStructuredConcurrency() }
            )

            Spacer(Modifier.height(4.dp))
            LogOutput(logs = logs)

            InterviewCard(
                questions = listOf(
                    "Q: launch 和 async 的差別是什麼？\n  A: launch = fire-and-forget (Job)；async = 可取值 (Deferred<T>，需 .await())",
                    "Q: withContext 和 launch 的差別？\n  A: withContext 是同步等待（suspends caller）；launch 建立子 Coroutine 立即返回",
                    "Q: Structured Concurrency 有什麼好處？\n  A: 確保沒有 leaked coroutine；父取消時所有子也取消；例外向上傳播"
                )
            )
        }
    }
}
