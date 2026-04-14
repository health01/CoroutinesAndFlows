package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 2: Cancellation & Exception Handling
 * 涵蓋：Job、SupervisorJob、CoroutineExceptionHandler、CancellationException
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: 如何安全取消一個 Coroutine？
 *     How do you safely cancel a coroutine?
 * A1: 呼叫 job.cancel()。但 cancel 只是設定取消旗標，
 *     Coroutine 必須到達 suspend point（如 delay、yield）才真正停止。
 *     Call job.cancel(). Cancellation only sets a flag; the coroutine must reach
 *     a suspension point (delay, yield, etc.) to actually stop.
 *     若有 CPU 密集迴圈，要手動檢查 isActive。
 *     For CPU-bound loops, manually check isActive.
 *
 * Q2: SupervisorJob 和普通 Job 的差異？
 *     What is the difference between SupervisorJob and regular Job?
 * A2: 普通 Job：任何子 Coroutine 失敗 → 所有兄弟及父都被取消。
 *     SupervisorJob：子失敗不影響兄弟，父也不被取消。
 *     Regular Job: one child fails → siblings and parent are cancelled.
 *     SupervisorJob: child failures are isolated; siblings continue.
 *
 * Q3: CoroutineExceptionHandler 一定能捕捉到所有例外嗎？
 *     Does CoroutineExceptionHandler catch all exceptions?
 * A3: 不！它只對 root coroutine（直接用 launch 啟動，且不在 coroutineScope 內）有效。
 *     async 的例外要在 .await() 處理；coroutineScope 內部的例外會直接傳播。
 *     No! It only works for root coroutines launched with launch.
 *     async exceptions must be caught at .await(); coroutineScope propagates exceptions directly.
 */
@HiltViewModel
class CancellationViewModel @Inject constructor() : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var longRunningJob: Job? = null

    private fun log(msg: String) {
        _logs.update { it + "[${System.currentTimeMillis() % 10000}ms] $msg" }
    }

    fun clearLogs() { _logs.value = emptyList() }

    // ── Demo 1: Job Cancellation ──────────────────────────────────────────
    fun startLongRunningJob() {
        clearLogs()
        log("=== Job Cancellation demo ===")
        longRunningJob = viewModelScope.launch {
            log("Long job started. Call cancel() to stop.")
            repeat(20) { i ->
                // isActive 是 Coroutine 取消的協作點（CPU-bound 迴圈用）
                // isActive is the cooperative cancellation check for CPU-bound loops
                if (!isActive) {
                    log("Detected cancellation at iteration $i, stopping...")
                    return@launch
                }
                delay(300) // suspend point — cancellation happens here too
                log("  Working... step ${i + 1}/20")
            }
            log("Long job completed normally")
        }
    }

    fun cancelLongRunningJob() {
        longRunningJob?.cancel()
        log("⛔ cancel() called — job will stop at next suspend point")
    }

    // ── Demo 2: SupervisorJob ─────────────────────────────────────────────
    /**
     * 使用 supervisorScope：子 Coroutine 失敗不影響兄弟。
     * supervisorScope: one child's failure doesn't cancel siblings.
     */
    fun demoSupervisorJob() {
        clearLogs()
        log("=== SupervisorJob demo ===")
        viewModelScope.launch {
            supervisorScope {
                // Child 1 會失敗 / Child 1 will fail
                val child1 = launch {
                    delay(200)
                    log("Child 1: about to throw!")
                    throw IllegalStateException("Child 1 failed!")
                }

                // Child 2 應該繼續執行 / Child 2 should continue
                val child2 = launch {
                    delay(600)
                    log("✅ Child 2: completed successfully despite Child 1 failing!")
                }

                // 捕捉 Child 1 的例外 / Catch Child 1's exception
                runCatching { child1.join() }
                    .onFailure { log("❌ Caught Child 1 exception: ${it.message}") }

                child2.join()
                log("supervisorScope completed")
            }
        }
    }

    // ── Demo 3: CoroutineExceptionHandler ────────────────────────────────
    /**
     * CoroutineExceptionHandler 只對用 launch 啟動的 root coroutine 有效。
     * CoroutineExceptionHandler only works for root coroutines started with launch.
     *
     * ⚠️ 注意：此範例中子 coroutine 的例外會直接取消父 coroutine（因為沒有用 SupervisorJob），
     *    展示的是例外如何透過 structured concurrency 向上传播。
     * ⚠️ Note: The child's exception cancels the parent (no SupervisorJob), demonstrating
     *    how exceptions propagate upward through structured concurrency.
     */
    fun demoExceptionHandler() {
        clearLogs()
        log("=== CoroutineExceptionHandler demo ===")

        val handler = CoroutineExceptionHandler { context, throwable ->
            log("🔴 ExceptionHandler caught: ${throwable.message}")
            log("   Coroutine: $context")
        }

        // handler 必須傳給 root coroutine 的 launch / handler must be on the root launch
        viewModelScope.launch(handler) {
            log("Root coroutine started")
            launch {
                delay(300)
                log("Child coroutine: about to throw!")
                throw RuntimeException("Uncaught exception from child!")
            }
            delay(600)
            log("This line won't be reached if child throws without SupervisorJob")
        }
    }

    // ── Demo 4: try/catch vs CancellationException ────────────────────────
    /**
     * ⚠️ 重要：不要 catch CancellationException 並吞掉！
     * ⚠️ Important: never catch and swallow CancellationException!
     * 它是 Kotlin Coroutines 取消協作機制的一部分。
     */
    fun demoCancellationException() {
        clearLogs()
        log("=== CancellationException demo ===")
        val job = viewModelScope.launch {
            try {
                delay(1000)
                log("This won't print if cancelled")
            } catch (e: kotlinx.coroutines.CancellationException) {
                log("⚠️ CancellationException caught — must re-throw! (re-throwing...)")
                throw e  // 正確做法：重新拋出 / Correct: always re-throw
            } finally {
                log("finally block always runs — good for cleanup")
            }
        }
        viewModelScope.launch {
            delay(300)
            log("Cancelling job after 300ms...")
            job.cancel()
        }
    }
}
