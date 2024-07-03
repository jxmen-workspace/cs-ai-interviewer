package dev.jxmen.cs.ai.interviewer.domain.chat.service.adpater

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatCommandRepository: ChatCommandRepository,
) : ChatUseCase {
    @Transactional
    override fun add(
        subject: Subject,
        userSessionId: String,
        answer: String,
        nextQuestion: String,
        score: Int,
        chats: List<Chat>,
    ) {
        if (chats.isEmpty()) {
            addQuestion(subject, userSessionId)
        }

        addAnswer(subject, userSessionId, answer, score)
        addNextQuestion(subject, userSessionId, nextQuestion)
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

    private fun addQuestion(
        subject: Subject,
        userSessionId: String,
    ) {
        val chat =
            Chat(
                subject = subject,
                userSessionId = userSessionId,
                message = subject.question,
                chatType = ChatType.QUESTION,
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
