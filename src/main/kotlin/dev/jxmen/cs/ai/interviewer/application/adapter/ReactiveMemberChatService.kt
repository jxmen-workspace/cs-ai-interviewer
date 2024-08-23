package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ReactiveMemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.common.utils.GrantRoleMessageFactory
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class ReactiveMemberChatService(
    private val chatModel: ChatModel,
    private val chatAppender: ChatAppender,
) : ReactiveMemberChatUseCase {
    companion object {
        private val SCORE_REGEX = "답변에 대한 점수: (\\d+)점".toRegex()
    }

    override fun answerAsync(command: CreateSubjectAnswerCommand): Flux<ChatResponse> {
        // 1. 답변을 모두 사용하지 않았는지 확인
        val chats = Chats(command.chats)
        chats.validateNotUseAllAnswers()
        chats.validateMatchMember(command.member)

        // 2. 기존 채팅 목록으로 Message 리스트 만들기
        val messages = createPromptMessages(command)

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
                val score = extractScore(answerMessageBuilder.toString())
                chatAppender.addAnswerAndNextQuestion(
                    subject = command.subject,
                    member = command.member,
                    answer = command.answer,
                    chats = command.chats,
                    nextQuestion = answerMessageBuilder.toString(),
                    score = score,
                )
            }
    }

    private fun createPromptMessages(command: CreateSubjectAnswerCommand): List<Message> =
        when (command.chats.isEmpty()) {
            /**
             * 채팅이 비어있다면 권한을 생성하는 메시지를 넣어 반환한다)
             */
            true ->
                GrantRoleMessageFactory.create(
                    question = command.subject.question,
                    answer = command.answer,
                )

            /**
             * 채팅이 비어있지 않다면 채팅을 순회하며 메시지를 생성한다.
             *
             * 단, 채팅의 첫번째 메시지는 질문이므로 제외한다.
             */
            false -> {
                command.chats.drop(1).map {
                    when (it.content.chatType) {
                        ChatType.ANSWER -> UserMessage(it.content.message)
                        ChatType.QUESTION -> AssistantMessage(it.content.message)
                    }
                } + UserMessage(command.answer)
            }
        }

    private fun extractScore(nextQuestion: String): Int =
        SCORE_REGEX
            .find(nextQuestion)
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0
}
