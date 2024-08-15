package dev.jxmen.cs.ai.interviewer.adapter.output.persistence

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
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
