package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatQueryRepository
import org.springframework.stereotype.Service

@Service
class ChatQueryService(
    private val chatQueryRepository: ChatQueryRepository,
) : ChatQuery {
    override fun findBySubjectAndMember(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): List<JpaChat> = chatQueryRepository.findBySubjectAndMember(jpaSubject, jpaMember)
}
