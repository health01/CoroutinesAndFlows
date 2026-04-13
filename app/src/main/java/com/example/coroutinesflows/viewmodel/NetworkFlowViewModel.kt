package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.domain.usecase.post.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 6: Retrofit + Flow 網路請求
 * 涵蓋：Loading / Success / Error / Empty 完整狀態管理
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: 如何用 Flow 管理網路請求的 Loading/Error/Success 狀態？
 *     How to manage Loading/Error/Success states with Flow for network requests?
 * A1: 用 sealed class UiState<T>，搭配：
 *     .onStart { emit(UiState.Loading) }
 *     .catch   { emit(UiState.Error(it.message)) }
 *     .map     { UiState.Success(it) }
 *     This pattern cleanly separates all states and is fully reactive.
 *
 * Q2: 網路請求要在哪個 Dispatcher 執行？
 *     Which Dispatcher should network requests run on?
 * A2: Retrofit 的 suspend 函式已自動在 OkHttp 的 Dispatcher 執行，
 *     不需要手動 withContext(Dispatchers.IO)。
 *     Retrofit suspend functions already run off-main-thread via OkHttp's dispatcher.
 *     You do NOT need to manually wrap with withContext(Dispatchers.IO).
 *
 * Q3: 如何避免畫面旋轉（Configuration Change）重新發送網路請求？
 *     How to prevent re-fetching on screen rotation (config change)?
 * A3: StateFlow 儲存在 ViewModel 中，ViewModel 在 config change 時不被銷毀，
 *     所以畫面重建時 UI 可以直接收到 StateFlow 的當前值。
 *     StateFlow lives in ViewModel which survives config changes.
 *     UI reconnects to existing StateFlow — no re-fetch needed.
 */
@HiltViewModel
class NetworkFlowViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostsUiState>(PostsUiState.Idle)
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    /**
     * 載入文章列表。
     * 使用 Flow operators 鏈接 Loading → Success/Error 狀態轉換。
     *
     * Loads posts using a reactive Flow operator chain:
     * onStart (emit Loading) → catch (emit Error) → collect (emit Success)
     */
    fun loadPosts() {
        viewModelScope.launch {
            getPostsUseCase()
                .onStart {
                    _uiState.value = PostsUiState.Loading
                }
                .catch { throwable ->
                    _uiState.value = PostsUiState.Error(
                        message = throwable.message ?: "Network error",
                        throwable = throwable
                    )
                }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) {
                        PostsUiState.Empty
                    } else {
                        PostsUiState.Success(posts)
                    }
                }
        }
    }

    fun retry() = loadPosts()
}

// ── UI State ──────────────────────────────────────────────────────────────
sealed class PostsUiState {
    data object Idle : PostsUiState()
    data object Loading : PostsUiState()
    data object Empty : PostsUiState()
    data class Success(val posts: List<Post>) : PostsUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : PostsUiState()
}
