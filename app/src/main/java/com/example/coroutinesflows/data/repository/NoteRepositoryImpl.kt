package com.example.coroutinesflows.data.repository

import com.example.coroutinesflows.data.local.NoteDao
import com.example.coroutinesflows.data.local.NoteEntity
import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository 實作層：負責資料來源的協調（本例僅有本地 DB）。
 * Repository implementation: coordinates data sources (local DB only in this demo).
 *
 * 關鍵：在這裡做 Entity ↔ Domain Model 的轉換（Mapping）。
 * Key: performs Entity ↔ Domain Model mapping here, NOT in DAO or ViewModel.
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    /**
     * 將 DAO 的 Flow<List<NoteEntity>> 透過 .map 轉換為 Flow<List<Note>>。
     * Maps DAO's Flow<List<NoteEntity>> to Flow<List<Note>> using .map operator.
     *
     * 這個 .map 是 Flow operator，不是 Collection.map；它會在每次 emit 時執行。
     * This .map is a Flow operator (not Collection.map); it runs on every emission.
     */
    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addNote(note: Note): Long =
        noteDao.insertNote(note.toEntity())

    override suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note.toEntity())

    override suspend fun deleteAll() = noteDao.deleteAll()
}

// ── Extension mapping functions ────────────────────────────────────────────

private fun NoteEntity.toDomain() = Note(
    id = id, title = title, content = content, timestamp = timestamp
)

private fun Note.toEntity() = NoteEntity(
    id = id, title = title, content = content, timestamp = timestamp
)
