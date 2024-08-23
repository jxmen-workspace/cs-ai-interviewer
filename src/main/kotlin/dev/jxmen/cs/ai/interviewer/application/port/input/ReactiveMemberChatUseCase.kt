package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import org.springframework.ai.chat.model.ChatResponse
import reactor.core.publisher.Flux

/**
 * 멤버 채팅 유스케이스 - Flux, Mono 등 비동기 처리일 때 이 인터페이스를 사용한다.
 */
interface ReactiveMemberChatUseCase {
    fun answerAsync(command: CreateSubjectAnswerCommand): Flux<ChatResponse>
}
