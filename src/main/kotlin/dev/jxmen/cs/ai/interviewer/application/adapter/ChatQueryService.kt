package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service

@Service
class ChatQueryService(
    private val chatQueryRepository: ChatQueryRepository,
) : ChatQuery {
    override fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat> = chatQueryRepository.findBySubjectAndMember(subject, member)
}
