package dev.jxmen.cs.ai.interviewer.persistence.mapper

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import org.springframework.stereotype.Component

@Component
class ChatMapper {
    fun toDomains(
        jpaChats: List<JpaChat>,
        subject: Subject,
        member: Member,
    ): List<Chat> =
        jpaChats.map { jpaChat ->
            Chat(
                id = jpaChat.id,
                subject = subject,
                member = member,
                message = jpaChat.content.message,
                score = jpaChat.content.score,
                type = jpaChat.content.chatType,
                createdAt = jpaChat.createdAt,
                updatedAt = jpaChat.updatedAt,
            )
        }
}
