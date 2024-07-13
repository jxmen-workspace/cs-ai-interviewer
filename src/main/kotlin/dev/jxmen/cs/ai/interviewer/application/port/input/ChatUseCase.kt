package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatUseCase {

    @Deprecated("멤버로 변경 예정")
    fun add(
        subject: Subject,
        userSessionId: String,
        answer: String,
        nextQuestion: String,
        score: Int,
    )

    fun add(
        subject: Subject,
        member: Member,
        answer: String,
        nextQuestion: String,
        score: Int,
    )
}
