package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 4: StateFlow + SharedFlow
 * 關鍵概念：UI State（狀態）vs UI Event（一次性事件）
 * Key concept: UI State (persistent) vs UI Event (one-shot)
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: StateFlow 和 SharedFlow 的差異？各自適合什麼使用場景？
 *     What is the difference between StateFlow and SharedFlow?
 * A1: StateFlow：永遠有值（initial value required）、只保留最新值、
 *     新 collector 立即收到最新值。適合 UI State。
 *     SharedFlow：無初始值、可設定 replay cache、可廣播給多個 collector。
 *     適合一次性 Event（導航、顯示 Snackbar）。
 *     StateFlow: always has a value, only latest, new collectors get current value. → UI State
 *     SharedFlow: no initial value, configurable replay, broadcasts. → UI Events
 *
 * Q2: 為什麼要用 SharedFlow 而非 Channel 來發送一次性 UI 事件？
 *     Why use SharedFlow instead of Channel for one-shot UI events?
 * A2: SharedFlow 支援多個 collector（廣播），Channel 是點對點的。
 *     SharedFlow 可設定 replay = 0 確保事件不被重複消費。
 *     但 Channel 在沒有 collector 時不會丟失事件。
 *     SharedFlow supports multiple collectors (broadcast). Channel is point-to-point.
 *     SharedFlow with replay=0 ensures events aren't replayed on re-subscription.
 *
 * Q3: StateFlow 和 LiveData 最大的差別？
 *     What is the biggest difference between StateFlow and LiveData?
 * A3: StateFlow 是 Kotlin 原生、不依賴 Android SDK、在測試中無需 InstantTaskExecutorRule。
 *     collectAsStateWithLifecycle 取代了 observe，並且是 Lifecycle-aware 的。
 */
@HiltViewModel
class StateSharedFlowViewModel @Inject constructor() : ViewModel() {

    // ── 1. UI State with StateFlow ─────────────────────────────────────────
    /**
     * _uiState 是私有可變的；uiState 是公開唯讀的。
     * Pattern: private mutable backing property + public read-only exposure.
     * 這樣 UI 層無法直接修改狀態，只有 ViewModel 能修改。
     * This prevents UI from directly mutating state — only ViewModel can.
     */
    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    // ── 2. One-shot Events with SharedFlow ────────────────────────────────
    /**
     * replay = 0：不快取，新 collector 不會收到舊事件（適合導航等一次性動作）。
     * replay = 0: no cache, new collectors won't receive past events (good for navigation).
     * extraBufferCapacity = 1：確保發送方不被 backpressure 阻塞。
     * extraBufferCapacity = 1: ensures sender doesn't block on backpressure.
     */
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ── State operations ──────────────────────────────────────────────────

    fun increment() {
        // .update 是原子操作，適合並發場景 / .update is atomic, safe for concurrent use
        _uiState.update { current ->
            current.copy(count = current.count + 1)
        }
    }

    fun decrement() {
        _uiState.update { it.copy(count = it.count - 1) }
    }

    fun reset() {
        _uiState.update { CounterUiState() }
    }

    // ── Async state change example ────────────────────────────────────────
    fun loadDataWithDelay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // Simulate network call
            _uiState.update { it.copy(isLoading = false, count = 42) }
            // Emit a one-shot event after loading completes
            _events.emit(UiEvent.ShowSnackbar("Data loaded! Count set to 42"))
        }
    }

    // ── SharedFlow multi-collector demo ──────────────────────────────────
    fun broadcastMessage(message: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowSnackbar(message))
        }
    }

    fun triggerNavigation() {
        viewModelScope.launch {
            _events.emit(UiEvent.Navigate("detail_screen"))
        }
    }
}

// ── UI State data class ───────────────────────────────────────────────────
/**
 * UI 狀態用 data class 表示，方便 copy 做局部更新。
 * UI state as data class enables easy partial updates via copy().
 */
data class CounterUiState(
    val count: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ── UI Events sealed class ────────────────────────────────────────────────
/**
 * 一次性 UI 事件用 sealed class 表示，窮舉所有可能的事件類型。
 * One-shot UI events as sealed class — exhaustive enumeration of all event types.
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    data object HideKeyboard : UiEvent()
}
