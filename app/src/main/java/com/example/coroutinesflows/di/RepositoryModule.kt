package com.example.coroutinesflows.di

import com.example.coroutinesflows.data.repository.NoteRepositoryImpl
import com.example.coroutinesflows.data.repository.PostRepositoryImpl
import com.example.coroutinesflows.domain.repository.NoteRepository
import com.example.coroutinesflows.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 使用 @Binds（而非 @Provides）將介面綁定到實作類別。
 * Uses @Binds (not @Provides) to bind interface to implementation.
 *
 * @Binds 比 @Provides 更高效：Hilt 只需知道介面→實作的映射，
 * 不需要建立 Module 實例或執行函式。
 * @Binds is more efficient: Hilt only needs the interface→impl mapping,
 * no Module instance or function execution needed.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
