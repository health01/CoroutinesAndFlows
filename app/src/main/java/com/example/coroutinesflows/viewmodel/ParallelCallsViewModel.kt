package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinesflows.domain.model.UserWithPosts
import com.example.coroutinesflows.domain.repository.PostRepository
import com.example.coroutinesflows.domain.usecase.post.GetUserWithPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 7: Parallel & Sequential API Calls
 * 涵蓋：async/await 平行呼叫 vs 循序呼叫的效能比較
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: 如何發出多個平行 API 請求並等待全部完成？
 *     How to make multiple parallel API calls and wait for all of them?
 * A1: 使用 async/await + coroutineScope：
 *     coroutineScope {
 *       val a = async { api.callA() }
 *       val b = async { api.callB() }
 *       Result(a.await(), b.await())
 *     }
 *     ⚠️ 若其中一個 async 失敗，另一個會被取消（structured concurrency）。
 *     ⚠️ If one async fails, the other is cancelled (structured concurrency).
 *
 * Q2: async 失敗時怎麼處理？
 *     How to handle failures in async?
 * A2: 方法1：try-catch 包住整個 coroutineScope。
 *     方法2：用 supervisorScope + 個別 try-catch。
 *     方法3：awaitAll() 會在任何一個失敗時拋出第一個例外。
 *     Option 1: try-catch around coroutineScope.
 *     Option 2: supervisorScope + individual try-catch.
 *     Option 3: awaitAll() throws on first failure.
 *
 * Q3: 平行呼叫和循序呼叫分別適合什麼場景？
 *     When to use parallel vs sequential API calls?
 * A3: 平行：多個請求彼此獨立，沒有依賴關係（同時載入 User 和 Posts）。
 *     循序：第二個請求依賴第一個的結果（先取 User ID，再用 ID 取 Posts）。
 *     Parallel: independent requests with no dependency.
 *     Sequential: second request depends on first result.
 */
@HiltViewModel
class ParallelCallsViewModel @Inject constructor(
    private val getUserWithPostsUseCase: GetUserWithPostsUseCase,
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ParallelUiState>(ParallelUiState.Idle)
    val uiState: StateFlow<ParallelUiState> = _uiState.asStateFlow()

    private val _timingLogs = MutableStateFlow<List<String>>(emptyList())
    val timingLogs: StateFlow<List<String>> = _timingLogs.asStateFlow()

    private fun logTiming(msg: String) {
        _timingLogs.update { it + msg }
    }

    // ── Parallel fetch using UseCase (async/await internally) ────────────
    fun loadUserWithPostsParallel(userId: Int = 1) {
        _uiState.value = ParallelUiState.Loading
        _timingLogs.value = emptyList()

        viewModelScope.launch {
            val start = System.currentTimeMillis()
            logTiming("▶ Starting PARALLEL fetch for userId=$userId")

            runCatching {
                // async/await inside GetUserWithPostsUseCase
                getUserWithPostsUseCase(userId)
            }.onSuccess { result ->
                val elapsed = System.currentTimeMillis() - start
                logTiming("✅ Done in ${elapsed}ms (PARALLEL)")
                logTiming("   User: ${result.user.name}")
                logTiming("   Posts: ${result.posts.size} items")
                _uiState.value = ParallelUiState.Success(result)
            }.onFailure { e ->
                val elapsed = System.currentTimeMillis() - start
                logTiming("❌ Failed in ${elapsed}ms: ${e.message}")
                _uiState.value = ParallelUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── Sequential fetch for comparison ──────────────────────────────────
    /**
     * 循序呼叫：先取 User，再取 Posts（兩個請求的延遲相加）。
     * Sequential: fetch User first, then Posts (latencies add up).
     */
    fun loadUserWithPostsSequential(userId: Int = 1) {
        _uiState.value = ParallelUiState.Loading
        _timingLogs.value = emptyList()

        viewModelScope.launch {
            val start = System.currentTimeMillis()
            logTiming("▶ Starting SEQUENTIAL fetch for userId=$userId")

            runCatching {
                // Sequential: await user first, then posts
                val user = repository.getUserById(userId)
                logTiming("  User fetched (${System.currentTimeMillis() - start}ms)")
                val posts = repository.getPostsByUser(userId)
                logTiming("  Posts fetched (${System.currentTimeMillis() - start}ms)")
                UserWithPosts(user, posts)
            }.onSuccess { result ->
                val elapsed = System.currentTimeMillis() - start
                logTiming("✅ Done in ${elapsed}ms (SEQUENTIAL — slower!)")
                _uiState.value = ParallelUiState.Success(result)
            }.onFailure { e ->
                _uiState.value = ParallelUiState.Error(e.message ?: "Error")
            }
        }
    }

    // ── Multiple parallel calls with awaitAll ─────────────────────────────
    /**
     * 同時抓取多個 User 的資料（awaitAll 等待所有完成）。
     * Fetch multiple users concurrently using awaitAll.
     */
    fun loadMultipleUsers(userIds: List<Int> = listOf(1, 2, 3)) {
        _uiState.value = ParallelUiState.Loading
        _timingLogs.value = emptyList()

        viewModelScope.launch {
            val start = System.currentTimeMillis()
            logTiming("▶ Loading ${userIds.size} users in parallel...")

            runCatching {
                val deferreds = userIds.map { id ->
                    async { repository.getUserById(id) }
                }
                deferreds.map { it.await() }
            }.onSuccess { users ->
                val elapsed = System.currentTimeMillis() - start
                logTiming("✅ ${users.size} users loaded in ${elapsed}ms")
                users.forEach { logTiming("   → ${it.name} (${it.email})") }
                _uiState.value = ParallelUiState.MultiUserSuccess(
                    users.map { "${it.name}\n${it.email}" }
                )
            }.onFailure { e ->
                _uiState.value = ParallelUiState.Error(e.message ?: "Error")
            }
        }
    }
}

// ── UI State ──────────────────────────────────────────────────────────────
sealed class ParallelUiState {
    data object Idle : ParallelUiState()
    data object Loading : ParallelUiState()
    data class Success(val data: UserWithPosts) : ParallelUiState()
    data class MultiUserSuccess(val users: List<String>) : ParallelUiState()
    data class Error(val message: String) : ParallelUiState()
}
