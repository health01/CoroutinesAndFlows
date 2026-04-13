package com.example.coroutinesflows.data.repository

import com.example.coroutinesflows.data.remote.ApiService
import com.example.coroutinesflows.data.remote.dto.PostDto
import com.example.coroutinesflows.data.remote.dto.UserDto
import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.domain.model.User
import com.example.coroutinesflows.domain.repository.PostRepository
import javax.inject.Inject

/**
 * 網路 Repository 實作。
 * Network Repository implementation.
 *
 * 在 Repository 層呼叫 suspend API，由 Hilt 注入的 ApiService 執行網路請求。
 * Calls suspend API functions; the injected ApiService handles HTTP via OkHttp.
 *
 * ⚠️ 不需要手動 withContext(Dispatchers.IO)：
 *    Retrofit 的 suspend 函式已自動在後台執行緒完成 I/O。
 * ⚠️ No need for manual withContext(Dispatchers.IO):
 *    Retrofit suspend functions already execute I/O off the main thread.
 */
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override suspend fun getPosts(): List<Post> =
        apiService.getPosts().map { it.toDomain() }

    override suspend fun getPostsByUser(userId: Int): List<Post> =
        apiService.getPostsByUser(userId).map { it.toDomain() }

    override suspend fun getPostById(id: Int): Post =
        apiService.getPostById(id).toDomain()

    override suspend fun getUsers(): List<User> =
        apiService.getUsers().map { it.toDomain() }

    override suspend fun getUserById(id: Int): User =
        apiService.getUserById(id).toDomain()
}

// ── Extension mapping functions ────────────────────────────────────────────

private fun PostDto.toDomain() = Post(id = id, userId = userId, title = title, body = body)
private fun UserDto.toDomain() = User(id = id, name = name, username = username, email = email)
