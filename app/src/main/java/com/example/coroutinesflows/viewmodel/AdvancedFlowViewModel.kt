package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 9: Advanced Flow Best Practices
 * 涵蓋：backpressure、shareIn、stateIn、debounce、flatMapLatest、
 *        distinctUntilChanged、conflate、buffer
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: Flow 中的 backpressure 是什麼？如何處理？
 *     What is backpressure in Flow and how do you handle it?
 * A1: 當生產速度 > 消費速度時，稱為 backpressure。
 *     Flow 預設是 sequential（無 backpressure 問題）。
 *     處理策略：
 *     - buffer()：緩衝 N 個值，讓生產和消費在不同協程中跑。
 *     - conflate()：只保留最新值，跳過中間值（適合 UI 更新）。
 *     - collectLatest()：每次新值來時，取消正在進行的 collect block。
 *     buffer(): buffers N items, producer/consumer run concurrently.
 *     conflate(): keeps only latest, drops intermediate values.
 *     collectLatest(): cancels ongoing collect on new emission.
 *
 * Q2: shareIn 和 stateIn 的差別？
 *     What is the difference between shareIn and stateIn?
 * A2: shareIn → SharedFlow（無初始值，可 replay N 個）→ 適合 Event
 *     stateIn  → StateFlow（有初始值，只保留最新）→ 適合 UI State
 *     shareIn → SharedFlow (no initial value, configurable replay) → Events
 *     stateIn  → StateFlow (initial value required, only latest) → UI State
 *
 * Q3: debounce 和 throttleFirst 的差別？什麼場景用 debounce？
 *     When to use debounce? What is the difference from throttle?
 * A3: debounce：在最後一個事件後等待 N ms 才 emit（適合搜尋框輸入防抖）。
 *     throttle（無內建，需自行實作）：每隔 N ms 取第一個值。
 *     debounce: waits N ms after last event before emitting → search field input.
 *     throttle: takes first event every N ms (must implement manually in Kotlin Flow).
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AdvancedFlowViewModel @Inject constructor() : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun log(msg: String) { _logs.value = _logs.value + msg }
    fun clearLogs() { _logs.value = emptyList() }

    // ── 1. debounce — 搜尋防抖 ───────────────────────────────────────────
    /**
     * 模擬搜尋框：用戶快速輸入時不立即搜尋，
     * 等最後一次輸入 300ms 後才發出請求（節省 API 呼叫次數）。
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * debounce + distinctUntilChanged + flatMapLatest 是搜尋框的黃金組合：
     * 1. debounce: 輸入停頓 300ms 才繼續
     * 2. distinctUntilChanged: 相同值不重複觸發
     * 3. flatMapLatest: 新搜尋開始時取消上一個搜尋 Flow
     */
    val searchResults: StateFlow<List<String>> =
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    if (query.isBlank()) {
                        emit(emptyList())
                    } else {
                        emit(listOf("Searching for '$query'..."))
                        delay(500) // Simulate network
                        emit(listOf(
                            "Result 1 for '$query'",
                            "Result 2 for '$query'",
                            "Result 3 for '$query'"
                        ))
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    // ── 2. buffer vs conflate ─────────────────────────────────────────────
    fun demoBuffer() {
        clearLogs()
        log("=== buffer() demo ===")
        viewModelScope.launch {
            flow {
                for (i in 1..5) {
                    log("Producing $i...")
                    emit(i)
                    delay(100) // Fast producer
                }
            }
                .buffer(capacity = 3) // 讓生產者繼續跑，不等消費者
                .collect { value ->
                    delay(300)         // Slow consumer
                    log("Consumed: $value")
                }
            log("buffer demo complete")
        }
    }

    fun demoConflate() {
        clearLogs()
        log("=== conflate() demo — drops intermediate values ===")
        viewModelScope.launch {
            flow {
                for (i in 1..10) {
                    emit(i)
                    delay(50) // Very fast producer
                }
            }
                .conflate() // 只保留最新，中間值被丟棄
                .collect { value ->
                    delay(200) // Slow consumer
                    log("Collected (conflated): $value")
                }
            log("conflate demo complete — note missing values!")
        }
    }

    // ── 3. shareIn — 在 Repository 層共享 Flow ────────────────────────────
    /**
     * shareIn 讓一個 cold Flow 變成 hot SharedFlow，供多個 collector 共享。
     * 適合：Repository 層的 Flow 被多個 ViewModel 訂閱時，避免重複執行 DB / 網路查詢。
     *
     * shareIn converts a cold Flow to hot SharedFlow, sharing a single upstream.
     * Use in Repository when multiple ViewModels subscribe to the same data source.
     */
    private val expensiveDataSource = flow {
        log("Expensive data source: fetching... (only runs once with shareIn)")
        delay(500)
        for (i in 1..5) {
            emit("Item $i")
            delay(200)
        }
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        replay = 1  // 新 collector 會立即收到最後 1 個值
    )

    fun demoShareIn() {
        clearLogs()
        log("=== shareIn demo ===")
        // Collector 1
        viewModelScope.launch {
            expensiveDataSource.collect { log("Collector 1: $it") }
        }
        // Collector 2 — 共享同一個上游，不會重複執行
        viewModelScope.launch {
            delay(100) // Start slightly later
            expensiveDataSource.collect { log("Collector 2: $it") }
        }
    }

    // ── 4. distinctUntilChanged ───────────────────────────────────────────
    fun demoDistinctUntilChanged() {
        clearLogs()
        log("=== distinctUntilChanged demo ===")
        viewModelScope.launch {
            flow {
                listOf(1, 1, 2, 2, 2, 3, 2).forEach { emit(it) }
            }
                .distinctUntilChanged()
                .collect { log("Emitted: $it (consecutive duplicates filtered)") }
        }
    }
}
