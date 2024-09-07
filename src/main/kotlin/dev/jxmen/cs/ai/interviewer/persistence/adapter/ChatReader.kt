package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.mapper.ChatMapper
import dev.jxmen.cs.ai.interviewer.persistence.mapper.MemberMapper
import dev.jxmen.cs.ai.interviewer.persistence.mapper.SubjectMapper
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatQueryRepository
import org.springframework.stereotype.Component

@Component
class ChatReader(
    private val chatQueryRepository: ChatQueryRepository,
    private val chatMapper: ChatMapper,
    private val subjectMapper: SubjectMapper,
    private val memberMapper: MemberMapper,
) {
    fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): Chats {
        val jpaSubject = subjectMapper.toJpaEntity(subject)
        val jpaMember = memberMapper.toJpaEntity(member)

        val jpaChats = chatQueryRepository.findBySubjectAndMember(jpaSubject, jpaMember)

        return Chats(chatMapper.toDomains(jpaChats, subject, member))
    }
}
