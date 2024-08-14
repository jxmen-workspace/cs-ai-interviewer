package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface ChatArchiveQueryRepository {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<JpaChatArchive>
}
