package com.priveat.app.data.repository

import com.priveat.app.data.local.ChatDao
import com.priveat.app.data.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class ExpertChatRepository(private val chatDao: ChatDao) {
    fun observeMessages(): Flow<List<ChatMessageEntity>> = chatDao.observeMessages()

    suspend fun seedGreetingIfEmpty(name: String, greeting: String? = null) {
        if (chatDao.count() > 0) return
        chatDao.insert(
            ChatMessageEntity(
                senderType = SENDER_EXPERT,
                message = greeting?.takeIf { it.isNotBlank() }
                    ?: "Hello, I'm $name, your private AI nutrition assistant. I can review your local meal logs, safety history, and diet goals without sending your vault data to a PrivEat server."
            )
        )
    }

    suspend fun addUserMessage(message: String, imageUri: String? = null) {
        chatDao.insert(ChatMessageEntity(senderType = SENDER_USER, message = message, imageUri = imageUri))
    }

    suspend fun addExpertMessage(message: String) {
        chatDao.insert(ChatMessageEntity(senderType = SENDER_EXPERT, message = message))
    }

    suspend fun wipe() = chatDao.clear()

    companion object {
        const val SENDER_USER = "USER"
        const val SENDER_EXPERT = "EXPERT"
    }
}
