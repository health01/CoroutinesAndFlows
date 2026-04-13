package com.example.coroutinesflows.data.remote

import com.example.coroutinesflows.data.remote.dto.PostDto
import com.example.coroutinesflows.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API 介面。
 * Retrofit API interface — all functions are suspend for coroutine support.
 *
 * 使用 JSONPlaceholder (https://jsonplaceholder.typicode.com) 作為公開測試 API。
 * Uses JSONPlaceholder as a free public test API.
 *
 * ⚠️ 面試重點：Retrofit 的 suspend 函式內部由 OkHttp Dispatcher 執行，
 *    結果會自動切回呼叫者的 Coroutine Context（通常是 IO）。
 *    Retrofit suspend functions are dispatched by OkHttp and resume on the caller's context.
 */
interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @GET("posts")
    suspend fun getPostsByUser(@Query("userId") userId: Int): List<PostDto>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostDto

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto
}
