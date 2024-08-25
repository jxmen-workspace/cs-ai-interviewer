package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatQueryRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaChatQueryRepository :
    ChatQueryRepository,
    JpaRepository<Chat, Long> {
    @Query(
        """
            SELECT c
            FROM Chat c
            WHERE c.subject = :subject
            AND c.member = :member
            ORDER BY c.createdAt ASC
            """,
    )
    override fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat>
}
