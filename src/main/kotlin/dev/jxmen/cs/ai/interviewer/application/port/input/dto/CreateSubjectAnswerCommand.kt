package dev.jxmen.cs.ai.interviewer.application.port.input.dto

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

data class CreateSubjectAnswerCommandV2(
    val subject: Subject,
    val answer: String,
    val member: Member,
    val chats: List<Chat>,
)
