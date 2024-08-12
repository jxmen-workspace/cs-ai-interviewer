package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatQueryRepository {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat>
}
