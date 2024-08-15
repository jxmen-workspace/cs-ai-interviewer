package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatAppender(
    private val chatCommandRepository: ChatCommandRepository,
) {
    /**
     * 답변과 다음 질문을 추가한다. 이 작업은 원자성을 지켜야 한다.
     */
    @Transactional
    fun addAnswerAndNextQuestion(
        subject: Subject,
        member: Member,
        answer: String,
        nextQuestion: String,
        score: Int,
    ) {
        addAnswer(subject, member, answer, score)
        addNextQuestion(subject, member, nextQuestion)
    }

    private fun addNextQuestion(
        subject: Subject,
        member: Member,
        nextQuestion: String,
    ) {
        val question = Chat.createQuestion(subject, member, nextQuestion)
        chatCommandRepository.save(question)
    }

    private fun addAnswer(
        subject: Subject,
        member: Member,
        message: String,
        score: Int,
    ) {
        val answer = Chat.createAnswer(subject, member, message, score)
        chatCommandRepository.save(answer)
    }
}
