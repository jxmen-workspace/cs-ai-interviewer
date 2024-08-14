package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import org.springframework.stereotype.Component

@Component
class ChatRemover(
    private val chatCommandRepository: ChatCommandRepository,
) {
    fun removeAll(chats: List<Chat>) {
        chatCommandRepository.deleteAllInBatch(chats)
    }
}
