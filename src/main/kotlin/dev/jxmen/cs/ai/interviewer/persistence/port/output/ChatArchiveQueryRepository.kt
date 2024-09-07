package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject

interface ChatArchiveQueryRepository {
    fun findBySubjectAndMember(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): List<JpaChatArchive>
}
