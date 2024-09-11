package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.common.utils.MessageParser
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.mapper.MemberMapper
import dev.jxmen.cs.ai.interviewer.persistence.mapper.SubjectMapper
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatCommandRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatAppender(
    private val chatCommandRepository: ChatCommandRepository,
    private val messageParser: MessageParser,
    private val subjectMapper: SubjectMapper,
    private val memberMapper: MemberMapper,
) {
    @Transactional
    fun addAnswerAndNextQuestion(
        subject: Subject,
        member: Member,
        answer: String,
        chats: Chats,
        nextQuestion: String,
    ) {
        val jpaSubject = subjectMapper.toJpaEntity(subject)
        val jpaMember = memberMapper.toJpaEntity(member)

        if (chats.isEmpty()) {
            addFirstQuestion(jpaSubject, jpaMember)
        }

        val score = messageParser.parseScore(nextQuestion)
        addAnswer(jpaSubject, jpaMember, answer, score)
        addNextQuestion(jpaSubject, jpaMember, nextQuestion)
    }

    private fun addFirstQuestion(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ) {
        val firstQuestion = JpaChat.createFirstQuestion(jpaSubject, jpaMember)
        chatCommandRepository.save(firstQuestion)
    }

    private fun addAnswer(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
        message: String,
        score: Int,
    ) {
        val answer = JpaChat.createAnswer(jpaSubject, jpaMember, message, score)
        chatCommandRepository.save(answer)
    }

    private fun addNextQuestion(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
        nextQuestion: String,
    ) {
        val question = JpaChat.createQuestion(jpaSubject, jpaMember, nextQuestion)
        chatCommandRepository.save(question)
    }
}
