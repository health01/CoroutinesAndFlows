package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 3: Basic Flow
 * 涵蓋：cold flow、intermediate operators、terminal operators
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: Flow 和 LiveData 的主要差異是什麼？
 *     What are the main differences between Flow and LiveData?
 * A1: Flow 是 Kotlin 原生、不依賴 Android、可在純 JVM 使用；
 *     支援背壓（backpressure）、更豐富的 operators、可測試性更好。
 *     LiveData 是 Android 專屬、自動感知 Lifecycle。
 *     Flow = Kotlin-native, lifecycle-agnostic, rich operators, testable.
 *     LiveData = Android-specific, lifecycle-aware.
 *     現代最佳實踐：使用 StateFlow/SharedFlow + collectAsStateWithLifecycle。
 *
 * Q2: Cold Flow 和 Hot Flow 的差異？
 *     What is the difference between cold and hot Flow?
 * A2: Cold Flow：每個 collector 都有自己獨立的執行序列，
 *     只有在 collect 時才開始執行（如 flow { }、channelFlow）。
 *     Hot Flow：不管有沒有 collector 都在執行（StateFlow、SharedFlow）。
 *     Cold: each collector gets its own execution; starts only when collected.
 *     Hot: runs regardless of collectors (StateFlow, SharedFlow).
 *
 * Q3: Flow 中的 map、flatMapLatest、transform 各適用什麼場景？
 *     When to use map vs flatMapLatest vs transform in Flow?
 * A3: map: 1對1轉換。
 *     flatMapLatest: 每個值會啟動新 Flow，前一個 Flow 會被取消（適合搜尋防抖）。
 *     transform: 可以 emit 任意次數（0~N），彈性最高。
 */
@HiltViewModel
class BasicFlowViewModel @Inject constructor() : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun log(msg: String) {
        _logs.update { it + msg }
    }

    fun clearLogs() { _logs.value = emptyList() }

    // ── Demo 1: Cold Flow — 基本 flow builder ─────────────────────────────
    /**
     * flow { } 是最基本的 cold flow builder。
     * 每次 collect 都從頭執行。
     * flow { } is the basic cold flow builder; restarts for each collector.
     */
    fun demoColdFlow() {
        clearLogs()
        log("=== Cold Flow demo ===")
        val numberFlow = flow {
            log("Flow block started (cold — runs per collector)")
            for (i in 1..5) {
                delay(200)
                log("  Emitting $i")
                emit(i)
            }
        }

        log("Collecting Flow 1:")
        viewModelScope.launch {
            numberFlow.collect { value ->
                log("  Collector 1 received: $value")
            }
        }

        viewModelScope.launch {
            delay(100) // Start slightly after collector 1
            log("Collecting Flow 2 (same flow, independent execution):")
            numberFlow.collect { value ->
                log("  Collector 2 received: $value")
            }
        }
    }

    // ── Demo 2: Intermediate Operators ────────────────────────────────────
    fun demoIntermediateOperators() {
        clearLogs()
        log("=== Intermediate Operators demo ===")
        viewModelScope.launch {
            flow { for (i in 1..10) emit(i) }
                .filter { it % 2 == 0 }              // 只保留偶數 / keep evens
                .map { it * it }                      // 平方 / square
                .take(3)                              // 只取前 3 個 / take first 3
                .onEach { log("  onEach: $it") }      // side-effect, doesn't transform
                .onStart { log("Flow started") }
                .onCompletion { log("Flow completed") }
                .catch { e -> log("Error: ${e.message}") }
                .collect { log("  Collected: $it") }
        }
    }

    // ── Demo 3: transform operator ────────────────────────────────────────
    /**
     * transform 最靈活：可以對每個值 emit 任意次（0~N）。
     * transform is the most flexible: can emit 0..N values per upstream element.
     */
    fun demoTransform() {
        clearLogs()
        log("=== transform operator demo ===")
        viewModelScope.launch {
            flow { for (i in 1..3) emit(i) }
                .transform { value ->
                    emit("start_$value")   // emit 2 items per input
                    emit("end_$value")
                }
                .collect { log("  $it") }
        }
    }

    // ── Demo 4: Terminal Operators ────────────────────────────────────────
    fun demoTerminalOperators() {
        clearLogs()
        log("=== Terminal Operators demo ===")
        viewModelScope.launch {
            val source = flow { for (i in 1..5) emit(i) }

            // toList() — collect all to a List
            val list = source.toList()
            log("toList(): $list")

            // collect with lambda
            source.collect { log("collect: $it") }
        }
    }
}
