package dev.jxmen.cs.ai.interviewer.domain.subject.service.port

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

data class CreateSubjectAnswerCommand(
    val subject: Subject,
    val answer: String,
    val userSessionId: String,
)
