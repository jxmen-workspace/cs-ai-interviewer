package dev.jxmen.cs.ai.interviewer.domain.chat.service.adpater

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatQuery
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatQueryService(
    private val chatQueryRepository: ChatQueryRepository,
) : ChatQuery {
    @Transactional(readOnly = true)
    override fun findBySubjectAndUserSessionId(
        subject: Subject,
        userSessionId: String,
    ): List<Chat> = chatQueryRepository.findBySubjectAndUserSessionId(subject, userSessionId)
}
