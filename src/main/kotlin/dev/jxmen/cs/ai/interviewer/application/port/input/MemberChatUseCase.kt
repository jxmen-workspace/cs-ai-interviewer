package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.ai.chat.model.ChatResponse
import reactor.core.publisher.Flux

interface MemberChatUseCase {
    fun answerAsync(command: CreateSubjectAnswerCommand): Flux<ChatResponse>

    fun archive(
        chats: List<Chat>,
        member: Member,
        subject: Subject,
    ): Long
}
