package com.zakss.jkt48universe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: Long,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("sender_name")
    val senderName: String,
    val body: String,
    @SerialName("created_at")
    val createdAt: String
)
