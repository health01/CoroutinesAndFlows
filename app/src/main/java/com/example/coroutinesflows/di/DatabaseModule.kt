package com.example.coroutinesflows.di

import android.content.Context
import androidx.room.Room
import com.example.coroutinesflows.data.local.AppDatabase
import com.example.coroutinesflows.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module：提供 Room Database 和 DAO 的依賴。
 * Hilt Module: provides Room Database and DAO dependencies.
 *
 * @InstallIn(SingletonComponent::class) — 整個 App 生命週期內只有一個實例。
 * @InstallIn(SingletonComponent::class) — single instance for the entire app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "coroutines_flows_db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)  // Demo 用；正式環境請提供 Migration
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()
}
