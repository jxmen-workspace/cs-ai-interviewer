package dev.jxmen.cs.ai.interviewer.domain.chat

interface ChatCommandRepository {
    fun save(chat: Chat): Chat
}
