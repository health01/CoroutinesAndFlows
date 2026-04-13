package com.example.coroutinesflows.domain.usecase.post

import com.example.coroutinesflows.domain.model.UserWithPosts
import com.example.coroutinesflows.domain.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * 平行呼叫示範：同時抓取 User 和其 Posts，兩個 API 同時發出，
 * 等待兩者都完成後組合結果，比循序快一倍。
 *
 * Parallel API call demo: fires both requests concurrently using async/await.
 * Both requests run in parallel; we await both and combine results.
 *
 * 關鍵 API Key API:
 * - `coroutineScope { }`: 建立子 scope，內部的 async 都是 structured concurrency 的子 Job。
 *   Creates a child scope; all async blocks are structured child Jobs.
 * - `async { }`: 啟動可回傳值的 Coroutine；`.await()` 等待結果。
 *   Launches a value-returning Coroutine; `.await()` suspends until result is ready.
 *
 * ⚠️ 陷阱 Trap：如果用 `launch` 取代 `async`，就無法取得回傳值。
 * ⚠️ If you use `launch` instead of `async`, you can't get the return value.
 */
class GetUserWithPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(userId: Int): UserWithPosts = coroutineScope {
        // 同時發出兩個請求 / Fire both requests concurrently
        val userDeferred  = async { repository.getUserById(userId) }
        val postsDeferred = async { repository.getPostsByUser(userId) }

        // 等待兩者完成 / Await both — suspends until both are done
        UserWithPosts(
            user  = userDeferred.await(),
            posts = postsDeferred.await()
        )
    }
}
