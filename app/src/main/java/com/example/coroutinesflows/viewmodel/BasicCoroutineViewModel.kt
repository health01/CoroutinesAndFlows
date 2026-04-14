package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 1: Basic Coroutines
 * 涵蓋：launch、async/await、withContext、structured concurrency
 * Covers: launch, async/await, withContext, structured concurrency
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 * Q1: launch 和 async 的差別是什麼？
 *     What is the difference between launch and async?
 * A1: launch 啟動「fire-and-forget」Coroutine，回傳 Job（無法取值）。
 *     async 啟動可回傳值的 Coroutine，回傳 Deferred<T>，透過 .await() 取值。
 *     launch = fire-and-forget, returns Job.
 *     async = returns Deferred<T>, call .await() to get the result.
 *
 * Q2: withContext 和 launch 的差別？
 *     What is the difference between withContext and launch?
 * A2: withContext 是 suspend 函式，它「等待」切換 Dispatcher 後的程式碼完成才繼續。
 *     launch 建立子 Coroutine 並立即返回，不等待子 Coroutine 完成。
 *     withContext suspends until the block completes (synchronous-ish).
 *     launch creates a child coroutine and returns immediately (asynchronous).
 *
 * Q3: Structured Concurrency 是什麼？有什麼好處？
 *     What is Structured Concurrency and what are its benefits?
 * A3: 子 Coroutine 的生命週期受父 Scope 約束。父被取消時，所有子也被取消；
 *     子出現例外時，例外會向上傳播到父。確保不會有 leaked coroutine。
 *     Child coroutine lifetime is bounded by parent scope. Cancel parent = cancel all children.
 *     Exception propagates upward. No leaked coroutines.
 */
@HiltViewModel
class BasicCoroutineViewModel @Inject constructor() : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun log(msg: String) {
        _logs.update { it + "[${System.currentTimeMillis() % 10000}ms] $msg" }
    }

    fun clearLogs() { _logs.value = emptyList() }

    // ── Demo 1: launch ────────────────────────────────────────────────────
    /**
     * launch：fire-and-forget，viewModelScope 確保 ViewModel 被清除時自動取消。
     * launch: fire-and-forget; viewModelScope auto-cancels when ViewModel is cleared.
     */
    fun demoLaunch() {
        clearLogs()
        log("=== launch demo start ===")
        viewModelScope.launch {
            log("Coroutine A started on ${currentThread()}")
            delay(500)
            log("Coroutine A done after 500ms")
        }
        viewModelScope.launch {
            log("Coroutine B started on ${currentThread()}")
            delay(300)
            log("Coroutine B done after 300ms")
        }
        log("launch returns immediately — both coroutines run concurrently")
    }

    // ── Demo 2: async / await ─────────────────────────────────────────────
    /**
     * async 適合需要回傳值的並行操作。
     * async is for concurrent operations that return values.
     */
    fun demoAsyncAwait() {
        clearLogs()
        log("=== async/await demo start ===")
        viewModelScope.launch {
            log("Starting two async tasks concurrently...")
            val startTime = System.currentTimeMillis()

            val resultA: Deferred<String> = async {
                delay(600)
                "Result A (600ms)"
            }
            val resultB: Deferred<String> = async {
                delay(400)
                "Result B (400ms)"
            }

            // awaitAll 等待所有 Deferred 完成 / awaits all Deferreds
            val results = awaitAll(resultA, resultB)
            val elapsed = System.currentTimeMillis() - startTime

            log("Both done in ${elapsed}ms (not 1000ms!) — true parallel!")
            results.forEach { log("  → $it") }
        }
    }

    // ── Demo 3: withContext ───────────────────────────────────────────────
    /**
     * withContext 切換 Dispatcher，完成後自動切回原 Dispatcher。
     * withContext switches Dispatcher and resumes on the original after block completes.
     */
    fun demoWithContext() {
        clearLogs()
        log("=== withContext demo start ===")
        viewModelScope.launch(Dispatchers.Main) {
            log("On Main: ${currentThread()}")

            val data = withContext(Dispatchers.IO) {
                log("Switched to IO: ${currentThread()} — doing heavy I/O")
                delay(500) // Simulate disk/network I/O
                "Data from IO thread"
            }

            // 自動回到 Main / Auto-resumed on Main
            log("Back on Main: ${currentThread()}")
            log("Received: $data")
        }
    }

    // ── Demo 4: Structured Concurrency ────────────────────────────────────
    /**
     * 展示父 Coroutine 會等待所有子完成才結束。
     * Parent coroutine waits for all children before completing.
     */
    fun demoStructuredConcurrency() {
        clearLogs()
        log("=== Structured Concurrency demo start ===")
        viewModelScope.launch {
            log("Parent started")
            launch {
                delay(3000)
                log("Child 1 done (300ms)")
            }
            launch {
                delay(500)
                log("Child 2 done (500ms)")
            }
            // 父 Coroutine 在這裡等待所有子完成
            // Parent implicitly waits for both children before this line
            log("Parent block end (but actual completion waits for children)")
        }
        log("viewModelScope.launch returned — parent + children still running")
    }

    private fun currentThread() = Thread.currentThread().name
}
