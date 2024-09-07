package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject

interface ChatQueryRepository {
    fun findBySubjectAndMember(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): List<JpaChat>
}
