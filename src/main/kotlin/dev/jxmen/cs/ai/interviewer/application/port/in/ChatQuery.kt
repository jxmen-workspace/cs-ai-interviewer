package dev.jxmen.cs.ai.interviewer.application.port.`in`

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatQuery {
    fun findBySubjectAndUserSessionId(
        subject: Subject,
        userSessionId: String,
    ): List<Chat>
}
