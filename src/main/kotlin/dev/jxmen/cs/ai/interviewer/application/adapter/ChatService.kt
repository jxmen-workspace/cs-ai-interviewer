package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatCommandRepository: ChatCommandRepository,
) : ChatUseCase {
    @Deprecated("멤버로 변경 예정")
    @Transactional
    override fun add(
        subject: Subject,
        userSessionId: String,
        answer: String,
        nextQuestion: String,
        score: Int,
    ) {
        addAnswer(subject, userSessionId, answer, score)
        addNextQuestion(subject, userSessionId, nextQuestion)
    }

    override fun add(
        subject: Subject,
        member: Member,
        answer: String,
        nextQuestion: String,
        score: Int,
    ) {
        val answer = Chat.createAnswer(subject, member, answer, score)
        chatCommandRepository.save(answer)

        val question = Chat.createQuestion(subject, member, nextQuestion)
        chatCommandRepository.save(question)
    }

    private fun addAnswer(
        subject: Subject,
        userSessionId: String,
        answer: String,
        score: Int,
    ) {
        val chat =
            Chat(
                subject = subject,
                userSessionId = userSessionId,
                message = answer,
                chatType = ChatType.ANSWER,
                score = score,
            )

        chatCommandRepository.save(chat)
    }

    private fun addNextQuestion(
        subject: Subject,
        userSessionId: String,
        nextQuestion: String,
    ) {
        val chat =
            Chat(
                subject = subject,
                userSessionId = userSessionId,
                message = nextQuestion,
                chatType = ChatType.QUESTION,
            )

        chatCommandRepository.save(chat)
    }
}
