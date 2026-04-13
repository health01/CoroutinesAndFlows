package com.example.coroutinesflows.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.ui.components.DemoScaffold
import com.example.coroutinesflows.ui.components.InfoCard
import com.example.coroutinesflows.ui.components.InterviewCard
import com.example.coroutinesflows.viewmodel.NoteUiState
import com.example.coroutinesflows.viewmodel.RoomFlowViewModel

@Composable
fun RoomFlowScreen(
    onBack: () -> Unit,
    viewModel: RoomFlowViewModel = hiltViewModel()
) {
    val notesUiState by viewModel.notesUiState.collectAsStateWithLifecycle()
    val titleInput by viewModel.titleInput.collectAsStateWithLifecycle()
    val contentInput by viewModel.contentInput.collectAsStateWithLifecycle()

    DemoScaffold(title = "🗄️ Room + Flow 即時更新", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            InfoCard(
                """
                Room DAO 回傳 Flow<List<Entity>>：
                每次 INSERT/UPDATE/DELETE 後，Room 的 InvalidationTracker
                自動重新查詢並 emit 新資料，UI 即時更新。

                Room DAO returns Flow<List<Entity>>:
                Room's InvalidationTracker automatically re-queries and emits
                on any data change → UI updates in real time.

                .stateIn(WhileSubscribed(5000)) 策略：
                App 進背景 5 秒後停止收集，節省資源。
                """.trimIndent()
            )

            Spacer(Modifier.height(8.dp))

            // ── Add Note Form ─────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("新增筆記 Add Note", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("標題 Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = viewModel::onContentChange,
                        label = { Text("內容 Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.addNote() },
                        enabled = titleInput.isNotBlank()
                    ) {
                        Text("➕ 新增 Add Note")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Notes List ────────────────────────────────────────────────
            when (val state = notesUiState) {
                is NoteUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is NoteUiState.Empty -> {
                    Text(
                        "尚無筆記。新增一個試試！\nNo notes yet. Add one above!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is NoteUiState.Success -> {
                    Text(
                        "筆記列表 Notes (${state.notes.size})",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.notes, key = { it.id }) { note ->
                            NoteItem(
                                note = note,
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
                is NoteUiState.Error -> {
                    Text(
                        "⚠️ ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            InterviewCard(
                questions = listOf(
                    "Q: 為何 Room DAO 的 Flow 會自動更新？\n  A: Room 用 InvalidationTracker 監聽表格，有 INSERT/DELETE/UPDATE 就重新 emit",
                    "Q: stateIn 的 WhileSubscribed(5000) 是什麼意思？\n  A: 最後一個 collector 取消後 5 秒才停止上游，適合 Activity onStop 後短暫背景存活",
                    "Q: 為什麼要在 Repository 做 Entity → Domain Model 轉換？\n  A: 保持 Domain 層純淨，不依賴 Room/Android；方便測試和替換資料來源"
                )
            )
        }
    }
}

@Composable
private fun NoteItem(note: Note, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, fontWeight = FontWeight.SemiBold)
                if (note.content.isNotBlank()) {
                    Text(
                        note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
