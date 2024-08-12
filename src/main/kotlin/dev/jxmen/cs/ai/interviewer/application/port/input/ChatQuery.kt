package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatQuery {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat>
}
