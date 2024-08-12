package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatCommandRepository: ChatCommandRepository,
) : ChatUseCase {
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
}
