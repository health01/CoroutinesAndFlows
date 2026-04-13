package com.example.coroutinesflows.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note operations.
 *
 * 關鍵知識點 Key Points:
 * 1. `Flow<List<NoteEntity>>` — Room 會自動在資料有異動時重新 emit 新資料。
 *    Room automatically re-emits new data whenever the underlying table changes.
 * 2. Room DAO 的 suspend function 內部使用 withContext(Dispatchers.IO) 切換執行緒。
 *    Room's suspend functions internally switch to IO dispatcher — you don't need to.
 * 3. Flow 是 cold stream：只有被 collect 才開始監聽；Room DAO Flow 例外，
 *    它實際上透過 InvalidationTracker 持續監聽表格變化（屬於 hot 行為）。
 *    Flow from Room DAO behaves like a hot observable via InvalidationTracker.
 */
@Dao
interface NoteDao {

    /** 回傳 Flow：資料有任何異動時自動重新 emit。 */
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    /** suspend fun：一次性查詢，非 Flow。 */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
