package com.example.coroutinesflows.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 8: Testing Coroutines & Flow
 * 涵蓋：runTest、StandardTestDispatcher、advanceTimeBy、Turbine
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: 如何測試 Coroutine？runTest 做了什麼？
 *     How to test coroutines? What does runTest do?
 * A1: runTest 建立一個 TestScope 並使用虛擬時鐘（virtual time），
 *     delay() 不會真的等待，而是立即跳過（或用 advanceTimeBy 手動推進）。
 *     runTest creates a TestScope with virtual time; delay() is skipped or
 *     advanced manually with advanceTimeBy/advanceUntilIdle.
 *
 * Q2: Turbine 是什麼？為什麼用它測試 Flow？
 *     What is Turbine and why use it for Flow testing?
 * A2: Turbine 是 CashApp 的 Flow 測試函式庫，提供 flow.test { } DSL，
 *     可以用 awaitItem() / awaitComplete() / awaitError() 逐步驗證 Flow 的每個 emission。
 *     Turbine is a Flow testing library by CashApp.
 *     flow.test { awaitItem() / awaitComplete() } lets you assert each emission sequentially.
 *
 * Q3: StandardTestDispatcher 和 UnconfinedTestDispatcher 的差異？
 *     Difference between StandardTestDispatcher and UnconfinedTestDispatcher?
 * A3: StandardTestDispatcher：Coroutine 不自動執行，需要 advanceTimeBy 或 advanceUntilIdle 推進。
 *     UnconfinedTestDispatcher：Coroutine 立即執行，行為更接近真實但控制性較低。
 *     StandardTestDispatcher: coroutines don't run automatically — you control time.
 *     UnconfinedTestDispatcher: coroutines run eagerly, less control.
 *
 * ⚠️ 關鍵：必須在 @Before 呼叫 Dispatchers.setMain(testDispatcher)，
 *    讓 viewModelScope（內部使用 Dispatchers.Main）在測試中使用虛擬 dispatcher。
 * ⚠️ Key: Must call Dispatchers.setMain(testDispatcher) in @Before so that
 *    viewModelScope (which uses Dispatchers.Main internally) runs on the test dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BasicCoroutineViewModelTest {

    // StandardTestDispatcher 讓測試中的時間是虛擬的，delay 不會真正等待
    // StandardTestDispatcher: virtual time — delay() doesn't actually wait
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BasicCoroutineViewModel

    @Before
    fun setUp() {
        // ✅ 必須設定！讓 viewModelScope 的 coroutine 跑在 testDispatcher 上
        // ✅ Required: makes viewModelScope coroutines run on testDispatcher
        Dispatchers.setMain(testDispatcher)
        viewModel = BasicCoroutineViewModel()
    }

    @After
    fun tearDown() {
        // ✅ 測試後還原，避免污染其他測試
        // ✅ Restore after test to avoid polluting other tests
        Dispatchers.resetMain()
    }

    // ── Test 1: 初始狀態 Initial state ────────────────────────────────────
    @Test
    fun `initial logs should be empty`() = runTest(testDispatcher) {
        assertEquals(emptyList<String>(), viewModel.logs.value)
    }

    // ── Test 2: clearLogs ─────────────────────────────────────────────────
    @Test
    fun `clearLogs should empty the log list`() = runTest(testDispatcher) {
        viewModel.demoLaunch()
        advanceTimeBy(1000)

        viewModel.clearLogs()

        assertEquals(emptyList<String>(), viewModel.logs.value)
    }

    // ── Test 3: Turbine — collect all emissions ───────────────────────────
    /**
     * 使用 Turbine 的 .test { } DSL 驗證 StateFlow 的 emissions。
     * Use Turbine's .test { } DSL to validate StateFlow emissions.
     *
     * awaitItem() 等待下一個 emission 並回傳它。
     * awaitItem() suspends until the next emission and returns it.
     */
    @Test
    fun `demoLaunch should add logs over time`() = runTest(testDispatcher) {
        viewModel.logs.test {
            val initial = awaitItem()
            assertTrue(initial.isEmpty())

            viewModel.demoLaunch()
            // Let all coroutines finish (500ms delay + buffer)
            advanceUntilIdle()

            val firstLogs = awaitItem()
            assertTrue(firstLogs.isNotEmpty())
            assertTrue(firstLogs.any { it.contains("launch") })
            // Verify both coroutines completed
            assertTrue(firstLogs.any { it.contains("Coroutine A done") })
            assertTrue(firstLogs.any { it.contains("Coroutine B done") })

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test 4: Verify logs contain expected content ──────────────────────
    @Test
    fun `demoAsyncAwait should log parallel completion`() = runTest(testDispatcher) {
        viewModel.demoAsyncAwait()
        advanceUntilIdle()

        val logs = viewModel.logs.value
        assertTrue("Should log async start", logs.any { it.contains("async/await") })
        // Verify both parallel tasks completed
        assertTrue("Should contain Result A", logs.any { it.contains("Result A") })
        assertTrue("Should contain Result B", logs.any { it.contains("Result B") })
        assertTrue("Should log completion time", logs.any { it.contains("Both done in") })
    }

    // ── Test 5: withContext switches dispatcher ───────────────────────────
    @Test
    fun `demoWithContext should log dispatcher switch and result`() = runTest(testDispatcher) {
        viewModel.demoWithContext()
        advanceUntilIdle()

        val logs = viewModel.logs.value
        assertTrue("Should log withContext start", logs.any { it.contains("withContext demo") })
        assertTrue("Should log IO dispatcher switch", logs.any { it.contains("IO") })
        assertTrue("Should log received data", logs.any { it.contains("Data from IO") })
    }

    // ── Test 6: Structured concurrency waits for children ─────────────────
    @Test
    fun `demoStructuredConcurrency should wait for all children`() = runTest(testDispatcher) {
        viewModel.demoStructuredConcurrency()
        advanceUntilIdle()

        val logs = viewModel.logs.value
        assertTrue("Should log parent start", logs.any { it.contains("Structured Concurrency") })
        // Both children should complete
        assertTrue("Should log Child 1 done", logs.any { it.contains("Child 1 done") })
        assertTrue("Should log Child 2 done", logs.any { it.contains("Child 2 done") })
    }
}
