package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatQueryRepository : JpaRepository<JpaChat, Long> {
    @Query(
        """
            SELECT c
            FROM JpaChat c
            WHERE c.jpaSubject = :jpaSubject
            AND c.jpaMember = :jpaMember
            ORDER BY c.createdAt ASC
            """,
    )
    fun findBySubjectAndMember(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): List<JpaChat>
}
