package com.example.coroutinesflows.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity：對應資料庫的 notes 表格。
 * Room Entity: maps to the "notes" table in the SQLite database.
 *
 * ⚠️ 面試陷阱：Entity 是資料層的細節，不應直接暴露給 Domain / UI 層。
 * ⚠️ Interview trap: Entity is a data-layer detail. Never expose it directly to Domain/UI.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
