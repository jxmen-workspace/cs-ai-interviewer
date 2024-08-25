package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatQueryRepository {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat>
}
