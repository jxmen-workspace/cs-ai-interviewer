package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatUseCase {
    fun add(
        subject: Subject,
        member: Member,
        answer: String,
        nextQuestion: String,
        score: Int,
    )
}
