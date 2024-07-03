package dev.jxmen.cs.ai.interviewer.domain.chat.service.port

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatUseCase {
    fun add(
        subject: Subject,
        userSessionId: String,
        answer: String,
        nextQuestion: String,
        score: Int,
        chats: List<Chat>,
    )
}
