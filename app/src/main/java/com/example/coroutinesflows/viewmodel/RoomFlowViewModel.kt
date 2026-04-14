package com.example.coroutinesflows.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.domain.usecase.note.AddNoteUseCase
import com.example.coroutinesflows.domain.usecase.note.DeleteNoteUseCase
import com.example.coroutinesflows.domain.usecase.note.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════
 * Use Case 5: Room + Flow 即時資料更新
 * Key concept: stateIn converts Flow to StateFlow for UI consumption
 * ═══════════════════════════════════════════════════════════
 *
 * 📌 面試高頻題 Interview Questions:
 *
 * Q1: 為什麼 Room DAO 的 Flow 回傳值是 "Hot" 行為？
 *     Why does Room DAO's Flow behave like a hot observable?
 * A1: Room 透過 InvalidationTracker 監聽表格變化，
 *     每當資料庫有 INSERT/UPDATE/DELETE，就重新查詢並 emit。
 *     Room uses InvalidationTracker; it re-queries and emits whenever the table changes.
 *
 * Q2: stateIn 的三個參數各是什麼意思？
 *     What do the three parameters of stateIn mean?
 * A2: scope: 在哪個 CoroutineScope 中共享。
 *     started: SharingStarted 策略：
 *       - Eagerly: 立即開始，永不停止。
 *       - Lazily: 第一個 collector 出現時開始。
 *       - WhileSubscribed(5000): 最後一個 collector 取消 5 秒後停止（節省資源）。
 *     initialValue: StateFlow 的初始值（必填）。
 *     scope: which CoroutineScope to share in.
 *     started: when to start/stop sharing:
 *       WhileSubscribed(5000) is the recommended production strategy.
 *     initialValue: initial StateFlow value (required).
 *
 * Q3: 為什麼推薦 WhileSubscribed(5000) 而不是 Eagerly？
 *     Why is WhileSubscribed(5000) recommended over Eagerly?
 * A3: 當 App 進背景（Activity onStop）後 5 秒自動停止收集，
 *     節省資源；當 App 回到前景時重新開始。
 *     Stops collection 5 seconds after last subscriber (e.g., activity going to background),
 *     saving resources. Resumes when app returns to foreground.
 */
@HiltViewModel
class RoomFlowViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    /**
     * 將 Domain Flow 轉換為 NoteUiState 的 StateFlow。
     * Converts domain Flow to StateFlow<NoteUiState> for UI consumption.
     *
     * .catch: 攔截上游 Flow 的例外，emit 一個錯誤狀態（不會讓 App crash）。
     * .catch: intercepts upstream exceptions, emits error state (prevents crashes).
     *
     * .stateIn: 將 cold Flow 轉換為 hot StateFlow，讓多個 collector 共享同一個收集。
     * .stateIn: converts cold Flow to hot StateFlow, sharing a single upstream collection.
     */
    val notesUiState: StateFlow<NoteUiState> =
        getNotesUseCase()
            .map { notes ->
                if (notes.isEmpty()) NoteUiState.Empty
                else NoteUiState.Success(notes)
            }
            .catch { e -> emit(NoteUiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NoteUiState.Loading
            )

    // ── Input state for the add-note form ─────────────────────────────────
    private val _titleInput = MutableStateFlow("")
    val titleInput: StateFlow<String> = _titleInput.asStateFlow()

    private val _contentInput = MutableStateFlow("")
    val contentInput: StateFlow<String> = _contentInput.asStateFlow()

    fun onTitleChange(title: String) { _titleInput.value = title }
    fun onContentChange(content: String) { _contentInput.value = content }

    // ── CRUD operations ───────────────────────────────────────────────────
    fun addNote() {
        val title = _titleInput.value
        val content = _contentInput.value
        if (title.isBlank()) return

        viewModelScope.launch {
            runCatching {
                addNoteUseCase(title, content)
                _titleInput.value = ""
                _contentInput.value = ""
            }.onFailure { e ->
                // In production, emit to a _errorEvent SharedFlow for UI display.
                // For now, log the error so developers can see it during debugging.
                android.util.Log.e("RoomFlowViewModel", "Failed to add note: ${e.message}", e)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
        }
    }
}

// ── UI State ──────────────────────────────────────────────────────────────
sealed class NoteUiState {
    data object Loading : NoteUiState()
    data object Empty : NoteUiState()
    data class Success(val notes: List<Note>) : NoteUiState()
    data class Error(val message: String) : NoteUiState()
}
