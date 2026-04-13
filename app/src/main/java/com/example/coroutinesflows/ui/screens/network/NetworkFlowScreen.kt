package com.example.coroutinesflows.ui.screens.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.ui.components.ActionRow
import com.example.coroutinesflows.ui.components.DemoScaffold
import com.example.coroutinesflows.ui.components.ErrorMessage
import com.example.coroutinesflows.ui.components.InfoCard
import com.example.coroutinesflows.ui.components.InterviewCard
import com.example.coroutinesflows.ui.components.LoadingIndicator
import com.example.coroutinesflows.viewmodel.NetworkFlowViewModel
import com.example.coroutinesflows.viewmodel.PostsUiState

@Composable
fun NetworkFlowScreen(
    onBack: () -> Unit,
    viewModel: NetworkFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DemoScaffold(title = "🌐 Retrofit + Flow 網路請求", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            InfoCard(
                """
                Flow 狀態管理模式 Flow State Pattern:
                .onStart  { emit(Loading) }   → 開始時先顯示 Loading
                .catch    { emit(Error(it)) } → 例外時顯示錯誤
                .collect  { emit(Success) }   → 成功時顯示資料

                使用 JSONPlaceholder 作為公開測試 API
                Uses JSONPlaceholder as a free public test API
                (需要網路連線 / Requires internet connection)
                """.trimIndent()
            )

            Spacer(Modifier.height(8.dp))

            ActionRow(
                primaryLabel = "▶ 載入文章 Load Posts",
                onPrimary = { viewModel.loadPosts() }
            )

            Spacer(Modifier.height(8.dp))

            // ── State-driven UI ───────────────────────────────────────────
            when (val state = uiState) {
                is PostsUiState.Idle -> {
                    Text(
                        "按上方按鈕開始載入 / Press button to load",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is PostsUiState.Loading -> {
                    LoadingIndicator("載入文章中… Fetching posts…")
                }
                is PostsUiState.Empty -> {
                    Text("📭 No posts found", modifier = Modifier.padding(16.dp))
                }
                is PostsUiState.Success -> {
                    Text(
                        "✅ 載入 ${state.posts.size} 篇文章",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.posts.take(20)) { post ->
                            PostItem(post)
                        }
                    }
                }
                is PostsUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            InterviewCard(
                questions = listOf(
                    "Q: Retrofit suspend 函式需要手動 withContext(IO) 嗎？\n  A: 不需要。Retrofit 內部已透過 OkHttp Dispatcher 在後台執行緒完成 HTTP I/O",
                    "Q: 如何用 Flow 管理 Loading/Error/Success？\n  A: sealed class + .onStart { emit(Loading) } + .catch { emit(Error) } + .collect { emit(Success) }",
                    "Q: 如何防止 config change 重新發網路請求？\n  A: StateFlow 存在 ViewModel（不會被銷毀），UI 重建後訂閱到現有 StateFlow 的當前值"
                )
            )
        }
    }
}

@Composable
private fun PostItem(post: Post) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "#${post.id} ${post.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                post.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
