package com.example.coroutinesflows.domain.usecase.post

import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 將 suspend 函式包裝成 Flow，讓 UI 層可以統一用 Flow 處理 Loading/Success/Error。
 * Wraps a suspend function into a Flow so the UI can uniformly handle Loading/Success/Error.
 *
 * 常見模式 Common pattern:
 *   flow { emit(repository.getPosts()) }
 *   → 搭配 .map / .catch / .onStart 處理各種狀態
 */
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<List<Post>> = flow {
        emit(repository.getPosts())
    }
}
