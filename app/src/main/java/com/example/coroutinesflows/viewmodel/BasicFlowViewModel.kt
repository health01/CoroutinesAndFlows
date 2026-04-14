package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
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
 *     transform: 可以 emit 任意次數（0~N），且支援 suspend，彈性最高。
 *       例：對每個 userId 先 emit Loading，再 suspend 拿資料，再 emit Result —
 *       map 做不到，因為 map 只能回傳單一值且無法在中間 emit 中間狀態。
 *     transform: emit 0..N values per element, with suspend support.
 *       e.g. for each userId: emit Loading → suspend fetch → emit Result.
 *       map cannot do this — it returns exactly one value with no intermediate emits.
 */
@HiltViewModel
class BasicFlowViewModel @Inject constructor() : ViewModel() {

    // 用 List<String> 而非 String，是為了讓 UI 能顯示完整歷史紀錄。
    // StateFlow 靠 reference 比較來決定是否 emit：
    //   it + msg 每次產生新的 List（新 reference）→ StateFlow emit → UI 更新。
    // 不用 MutableList.add()，因為那會修改原本的 reference → StateFlow 不 emit → UI 不更新。
    //
    // Using List<String> (not String) so the UI can display the full log history.
    // StateFlow uses reference equality to decide whether to emit:
    //   it + msg creates a new List each time (new reference) → StateFlow emits → UI updates.
    // MutableList.add() mutates in place (same reference) → StateFlow does NOT emit → UI stale.
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun log(msg: String) {
//        _logs.update { currentList ->
//            (currentList as MutableList).add(msg)  // 修改原本的 list
//            currentList  // 回傳同一個 reference → StateFlow 不 emit

        // ✅ it + msg — 產生新 List，新 reference
        _logs.update { it + msg }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

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
     * transform 最靈活：可以對每個值 emit 任意次（0~N），且可在中間執行 suspend 操作。
     * transform is the most flexible: emit 0..N values per element, with suspend support.
     *
     * 實戰場景：對每個 userId，先 emit Loading，再 suspend 拿資料，再 emit Result。
     * Real-world: for each userId, emit Loading first, then suspend-fetch, then emit Result.
     * map 做不到這件事，因為 map 只能回傳單一值且無法在中間 emit。
     * map cannot do this — it returns exactly one value and cannot emit intermediate states.
     */
    fun demoTransform() {
        clearLogs()
        log("=== transform operator demo ===")
        viewModelScope.launch {
            flow { for (id in 1..3) emit(id) }  // upstream: user IDs 1, 2, 3
                .transform { userId ->
                    emit("⏳ Loading user $userId...")   // emit 1: loading state
                    delay(400)                           // simulate network call
                    emit("✅ User $userId loaded")       // emit 2: result
                }
                .collect { log("  $it") }
        }
    }

    // ── Demo 4: Terminal Operators ────────────────────────────────────────
    /**
     * Terminal operator 是啟動 Flow 執行的觸發點。
     * Flow 是 cold 的 — 在呼叫 terminal operator 之前，整個 pipeline 什麼都不會執行。
     * Terminal operators are what actually start a Flow executing.
     * Flow is cold — nothing runs until a terminal operator is called.
     *
     * Intermediate operators (map, filter, transform) 回傳 Flow<T>，不啟動執行。
     * Terminal operators (collect, toList, first) 回傳實際值，且是 suspend function，
     * 必須在 coroutine 內呼叫。
     *
     * Intermediate operators (map, filter, transform) return Flow<T> — no execution yet.
     * Terminal operators (collect, toList, first) return a real value and are suspend functions
     * — must be called inside a coroutine.
     */
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
