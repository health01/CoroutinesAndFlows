package com.example.coroutinesflows.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database 宣告。
 * Room Database declaration.
 *
 * - version: Schema 版本；每次變更 Entity 結構需遞增並提供 Migration。
 *   version: Schema version. Must be incremented with each Entity change + Migration provided.
 * - exportSchema = true（建議）: 將 schema 匯出為 JSON 供版本控管。
 *   exportSchema = true (recommended): exports schema JSON for version control.
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
