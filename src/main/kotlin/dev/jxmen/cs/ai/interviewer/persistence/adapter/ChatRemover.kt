package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatCommandRepository
import org.springframework.stereotype.Component

@Component
class ChatRemover(
    private val chatCommandRepository: ChatCommandRepository,
) {
    fun removeAll(jpaChats: List<JpaChat>) {
        chatCommandRepository.deleteAllInBatch(jpaChats)
    }
}
