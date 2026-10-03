package com.example.data.model

import java.util.UUID

enum class MessageSender {
    PATIENT,
    AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val quickReplies: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
