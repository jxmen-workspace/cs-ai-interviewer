package dev.jxmen.cs.ai.interviewer.application.port.input.dto

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

data class CreateSubjectAnswerCommand(
    val subject: Subject,
    val answer: String,
    val userSessionId: String,
)

data class CreateSubjectAnswerCommandV2(
    val subject: Subject,
    val answer: String,
    val member: Member
)
