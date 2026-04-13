package com.example.coroutinesflows.viewmodel

import app.cash.turbine.test
import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.domain.usecase.post.GetPostsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 測試 NetworkFlowViewModel — MockK + Turbine + StandardTestDispatcher
 *
 * ⚠️ 為什麼用 StandardTestDispatcher 而非 UnconfinedTestDispatcher？
 *    Why StandardTestDispatcher instead of UnconfinedTestDispatcher?
 *
 * StateFlow 的衝突（conflation）機制：如果 Loading → Success 在同一個 coroutine
 * continuation 中發生（無任何 suspension point），下游 collector（Turbine）可能
 * 只看到最終的 Success，而 Loading 被跳過。
 * StateFlow conflation: if Loading→Success happens within a single coroutine
 * continuation (no suspension), the downstream collector (Turbine) may only
 * see the final Success, and Loading is dropped.
 *
 * StandardTestDispatcher 在每個 suspension point 後讓 scheduler 調度其他
 * coroutine，給 Turbine 機會在 Success 發出前先收到 Loading。
 * StandardTestDispatcher pauses after each suspension, allowing Turbine's
 * collector to receive Loading before Success is emitted.
 *
 * 關鍵：runTest(testDispatcher) 讓 test scope 和 viewModelScope 共用同一個
 * scheduler，確保時鐘推進能同時推進所有 coroutine。
 * Key: runTest(testDispatcher) makes the test scope and viewModelScope share
 * the same scheduler — time advancement drives all coroutines in sync.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkFlowViewModelTest {

    // ✅ 單一 dispatcher 實例，供 setMain 和 runTest 共用
    // ✅ Single dispatcher instance shared by setMain and runTest
    private val testDispatcher = StandardTestDispatcher()

    private val mockGetPostsUseCase: GetPostsUseCase = mockk()
    private lateinit var viewModel: NetworkFlowViewModel

    private val fakePosts = listOf(
        Post(id = 1, userId = 1, title = "Test Post 1", body = "Body 1"),
        Post(id = 2, userId = 1, title = "Test Post 2", body = "Body 2")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NetworkFlowViewModel(mockGetPostsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Test: initial state is Idle ───────────────────────────────────────
    @Test
    fun `initial state should be Idle`() = runTest(testDispatcher) {
        assertEquals(PostsUiState.Idle, viewModel.uiState.value)
    }

    // ── Test: loadPosts success path ──────────────────────────────────────
    @Test
    fun `loadPosts should emit Loading then Success`() = runTest(testDispatcher) {
        every { mockGetPostsUseCase() } returns flowOf(fakePosts)

        viewModel.uiState.test {
            assertEquals(PostsUiState.Idle, awaitItem())

            viewModel.loadPosts()

            // StandardTestDispatcher: scheduler advances at each suspension,
            // so Turbine captures Loading before Success is emitted
            assertEquals(PostsUiState.Loading, awaitItem())

            val success = awaitItem() as PostsUiState.Success
            assertEquals(2, success.posts.size)
            assertEquals("Test Post 1", success.posts.first().title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test: loadPosts error path ────────────────────────────────────────
    @Test
    fun `loadPosts should emit Loading then Error on network failure`() = runTest(testDispatcher) {
        val errorMessage = "Network error: 404"
        every { mockGetPostsUseCase() } returns flow {
            throw RuntimeException(errorMessage)
        }

        viewModel.uiState.test {
            assertEquals(PostsUiState.Idle, awaitItem())

            viewModel.loadPosts()

            assertEquals(PostsUiState.Loading, awaitItem())

            val error = awaitItem() as PostsUiState.Error
            assertTrue(error.message.contains(errorMessage))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test: empty posts ─────────────────────────────────────────────────
    @Test
    fun `loadPosts should emit Empty when no posts returned`() = runTest(testDispatcher) {
        every { mockGetPostsUseCase() } returns flowOf(emptyList())

        viewModel.uiState.test {
            assertEquals(PostsUiState.Idle, awaitItem())
            viewModel.loadPosts()
            assertEquals(PostsUiState.Loading, awaitItem())
            assertEquals(PostsUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test: retry calls loadPosts again ─────────────────────────────────
    @Test
    fun `retry should re-trigger loadPosts`() = runTest(testDispatcher) {
        every { mockGetPostsUseCase() } returns flowOf(fakePosts)

        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.retry()

            awaitItem() // Loading
            val result = awaitItem()
            assertTrue(result is PostsUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
