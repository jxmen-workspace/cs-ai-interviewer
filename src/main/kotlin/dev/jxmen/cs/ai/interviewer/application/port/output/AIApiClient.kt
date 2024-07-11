package dev.jxmen.cs.ai.interviewer.application.port.output

import dev.jxmen.cs.ai.interviewer.application.port.output.dto.AiApiAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface AIApiClient {
    fun requestAnswer(
        subject: Subject,
        answer: String,
        chats: List<Chat>,
    ): AiApiAnswerResponse
}
