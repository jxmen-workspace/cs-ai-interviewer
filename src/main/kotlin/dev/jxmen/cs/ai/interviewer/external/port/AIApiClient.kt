package dev.jxmen.cs.ai.interviewer.external.port

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface AIApiClient {
    fun requestAnswer(
        subject: Subject,
        answer: String,
        chats: List<Chat>,
    ): AiApiAnswerResponse
}
