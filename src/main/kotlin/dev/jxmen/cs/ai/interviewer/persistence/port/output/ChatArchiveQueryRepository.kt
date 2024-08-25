package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive

interface ChatArchiveQueryRepository {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<JpaChatArchive>
}
