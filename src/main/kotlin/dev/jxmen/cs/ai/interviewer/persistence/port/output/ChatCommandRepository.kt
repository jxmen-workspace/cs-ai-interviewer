package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat

interface ChatCommandRepository {
    fun save(chat: Chat): Chat

    fun deleteAllInBatch(chats: Iterable<Chat>)
}
