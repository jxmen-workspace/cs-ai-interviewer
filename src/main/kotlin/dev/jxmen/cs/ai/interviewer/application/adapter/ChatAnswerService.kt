package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatAnswerUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.common.utils.PromptMessageFactory
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatAppender
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatReader
import dev.jxmen.cs.ai.interviewer.persistence.adapter.SubjectReader
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class ChatAnswerService(
    private val subjectReader: SubjectReader,
    private val chatReader: ChatReader,
    private val promptMessageFactory: PromptMessageFactory,
    private val chatModel: ChatModel,
    private val chatAppender: ChatAppender,
) : ChatAnswerUseCase {
    override fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse> {
        val subject = subjectReader.findById(command.subjectId)
        val chats = chatReader.findBySubjectAndMember(subject, command.member)

        // 1. 답변을 모두 사용하지 않았는지 확인
        chats.validateNotUseAllAnswers()

        // 2. 기존 채팅 목록으로 Message 리스트 만들기
        val messages = promptMessageFactory.create(command.answer, chats, subject)

        // 3. 챗 모델 호출하고 마지막 결과 저장
        val answerMessageBuilder = StringBuilder()
        return chatModel
            .stream(Prompt(messages))
            .doOnNext {
                it.results.forEach {
                    answerMessageBuilder.append(it.output.content)
                }
            }
            // 차단되지 않는 컨텍스트에서 호출을 차단하면 스레드 고갈이 발생할 수 있습니다.
            .publishOn(Schedulers.boundedElastic())
            .doOnComplete {
                chatAppender.addAnswerAndNextQuestion(
                    subject = subject,
                    member = command.member,
                    answer = command.answer,
                    chats = chats,
                    nextQuestion = answerMessageBuilder.toString(),
                )
            }
    }
}
