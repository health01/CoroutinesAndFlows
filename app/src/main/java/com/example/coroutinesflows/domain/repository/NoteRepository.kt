package com.example.coroutinesflows.domain.repository

import com.example.coroutinesflows.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository 介面定義在 Domain 層：
 * 讓 Domain（UseCase）依賴抽象而非具體實作（依賴倒置原則）。
 *
 * Repository interface lives in the Domain layer:
 * Domain depends on abstractions, not concrete implementations (Dependency Inversion).
 */
interface NoteRepository {
    /** Cold Flow：collect 時才開始，資料異動時自動重新 emit。 */
    fun observeNotes(): Flow<List<Note>>

    suspend fun addNote(note: Note): Long

    suspend fun deleteNote(note: Note)

    suspend fun deleteAll()
}
