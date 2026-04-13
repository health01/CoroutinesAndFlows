package com.example.coroutinesflows.data.remote.dto

import com.google.gson.annotations.SerializedName

/** API 回應的資料類別（DTO = Data Transfer Object）。 */
data class PostDto(
    @SerializedName("id")     val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title")  val title: String,
    @SerializedName("body")   val body: String
)
