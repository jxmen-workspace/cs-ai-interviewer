package dev.jxmen.cs.ai.interviewer.application.port.input.dto

import dev.jxmen.cs.ai.interviewer.domain.member.Member

data class CreateSubjectAnswerCommand2(
    val subjectId: Long,
    val member: Member,
    val answer: String,
)
