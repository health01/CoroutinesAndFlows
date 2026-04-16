package com.example.coroutinesflows.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
        // 單一 LazyColumn：避免外層 Column 裡 weight(1f) 的 LazyColumn 與底部 InterviewCard
        // 搶高度時，列表被量到 0.dp 而看不到 NoteItem。
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
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
            }

            item {
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
            }

            when (val state = notesUiState) {
                is NoteUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is NoteUiState.Empty -> {
                    item {
                        Text(
                            "尚無筆記。新增一個試試！\nNo notes yet. Add one above!",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is NoteUiState.Success -> {
                    item {
                        Text(
                            "筆記列表 Notes (${state.notes.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(
                        items = state.notes,
                        key = { it.id },
                        contentType = { _ -> "note" }
                    ) { note ->
                        NoteItem(
                            note = note,
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
                is NoteUiState.Error -> {
                    item {
                        Text(
                            "⚠️ ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
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
}

@Composable
private fun NoteItem(note: Note, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
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
