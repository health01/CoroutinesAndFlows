package com.example.coroutinesflows.domain.repository

import com.example.coroutinesflows.domain.model.Post
import com.example.coroutinesflows.domain.model.User

interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun getPostsByUser(userId: Int): List<Post>
    suspend fun getPostById(id: Int): Post
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User
}
