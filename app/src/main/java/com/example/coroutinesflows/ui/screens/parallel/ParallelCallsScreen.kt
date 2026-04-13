package com.example.coroutinesflows.ui.screens.parallel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.example.coroutinesflows.viewmodel.ParallelCallsViewModel
import com.example.coroutinesflows.viewmodel.ParallelUiState

@Composable
fun ParallelCallsScreen(
    onBack: () -> Unit,
    viewModel: ParallelCallsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timingLogs by viewModel.timingLogs.collectAsStateWithLifecycle()

    DemoScaffold(title = "⚡ 平行 & 順序 API 呼叫", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                """
                平行呼叫 Parallel (async/await):
                  val user  = async { getUserById(1) }   // 同時發出
                  val posts = async { getPostsByUser(1) } // 同時發出
                  UserWithPosts(user.await(), posts.await()) // 等兩者完成
                  → 總時間 ≈ max(A, B)，而非 A + B

                循序呼叫 Sequential:
                  val user  = getUserById(1)     // 等待
                  val posts = getPostsByUser(1)  // 再等待
                  → 總時間 ≈ A + B（較慢）

                awaitAll 多重平行:
                  listOf(1,2,3).map { async { getUserById(it) } }.awaitAll()
                """.trimIndent()
            )

            ActionRow(
                primaryLabel = "▶ 平行 Parallel (faster)",
                onPrimary = { viewModel.loadUserWithPostsParallel() }
            )

            ActionRow(
                primaryLabel = "▶ 循序 Sequential (slower)",
                onPrimary = { viewModel.loadUserWithPostsSequential() }
            )

            ActionRow(
                primaryLabel = "▶ 多重平行 awaitAll",
                onPrimary = { viewModel.loadMultipleUsers() }
            )

            // ── Timing Log ────────────────────────────────────────────────
            LogOutput(logs = timingLogs)

            // ── Result Display ────────────────────────────────────────────
            when (val state = uiState) {
                is ParallelUiState.Idle -> {}
                is ParallelUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is ParallelUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✅ 結果 Result", fontWeight = FontWeight.Bold)
                            Text("User: ${state.data.user.name} (${state.data.user.email})")
                            Text("Posts: ${state.data.posts.size} items loaded")
                        }
                    }
                }
                is ParallelUiState.MultiUserSuccess -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✅ 多位使用者 Multiple Users", fontWeight = FontWeight.Bold)
                            state.users.forEach { Text("• $it") }
                        }
                    }
                }
                is ParallelUiState.Error -> {
                    Text("❌ ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(4.dp))
            InterviewCard(
                questions = listOf(
                    "Q: 如何同時發出兩個 API 請求並等待全部完成？\n  A: coroutineScope { val a=async{...}; val b=async{...}; Pair(a.await(),b.await()) }",
                    "Q: async 中一個失敗時另一個會怎樣？\n  A: 在 coroutineScope 中，任一 async 失敗 → 整個 scope 取消（structured concurrency）；若要隔離用 supervisorScope",
                    "Q: awaitAll 和逐個 .await() 的差異？\n  A: awaitAll 等待所有完成並回傳 List；逐個 .await() 按順序等，行為相同但語意更清晰"
                )
            )
        }
    }
}
