package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatQueryRepository {
    fun findBySubjectAndUserSessionId(
        subject: Subject,
        userSessionId: String,
    ): List<Chat>
}
