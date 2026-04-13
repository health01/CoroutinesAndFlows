package com.example.coroutinesflows.domain.usecase.note

import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase 最佳實踐：
 * 1. 單一職責（Single Responsibility）：只做一件事。
 * 2. `operator fun invoke` 讓呼叫方式更簡潔：`getNotesUseCase()`。
 * 3. 回傳 Flow 讓 ViewModel 可以響應式訂閱資料。
 *
 * UseCase best practices:
 * 1. Single Responsibility: does exactly one thing.
 * 2. `operator fun invoke` enables clean call syntax: `getNotesUseCase()`.
 * 3. Returns Flow so the ViewModel can reactively observe data.
 */
class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.observeNotes()
}
