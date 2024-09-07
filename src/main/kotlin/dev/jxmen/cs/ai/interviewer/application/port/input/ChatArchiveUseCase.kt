package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject

interface ChatArchiveUseCase {
    fun archive(
        jpaChats: List<JpaChat>,
        jpaMember: JpaMember,
        jpaSubject: JpaSubject,
    ): Long
}
