package com.example.coroutinesflows.domain.model

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String
)

/** 聚合模型：用於平行呼叫 Use Case 的回傳值。 */
data class UserWithPosts(
    val user: User,
    val posts: List<Post>
)
