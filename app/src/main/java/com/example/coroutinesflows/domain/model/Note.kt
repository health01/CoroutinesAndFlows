package com.example.coroutinesflows.domain.model

/**
 * Domain Model：純 Kotlin 資料類別，不依賴任何 Android / 框架類別。
 * Domain Model: pure Kotlin data class, zero dependency on Android or framework classes.
 */
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
