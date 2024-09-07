package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand2
import org.springframework.ai.chat.model.ChatResponse
import reactor.core.publisher.Flux

/**
 * 멤버 채팅 답변 처리를 위한 UseCase
 */
interface ChatAnswerUseCase {
    @Deprecated("Use answer(CreateSubjectAnswerCommand2) instead")
    fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse>

    fun answerV6(command: CreateSubjectAnswerCommand2): Flux<ChatResponse>
}
