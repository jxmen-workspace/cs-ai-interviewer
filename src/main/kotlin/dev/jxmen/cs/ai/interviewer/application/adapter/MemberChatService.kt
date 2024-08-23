package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.common.utils.GrantRoleMessageFactory
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class MemberChatService(
    // NOTE: 채팅 제거, 채팅 아카아브 생성을 port로 만들면 너무 이른 추상화라 판단되어 구현체 클래스를 직업 의존하도록 하였다.
    private val chatModel: ChatModel,
    private val chatAppender: ChatAppender,
    private val chatRemover: ChatRemover,
    private val chatArchiveAppender: ChatArchiveAppender,
) : MemberChatUseCase {
    companion object {
        private val scoreRegex = "답변에 대한 점수: (\\d+)점".toRegex()
    }

    override fun answerAsync(command: CreateSubjectAnswerCommand): Flux<ChatResponse> {
        // 1. 답변을 모두 사용하지 않았는지 확인
        validateNotUseAllAnswers(command.chats)

        // 2. 기존 채팅 목록으로 Message 리스트 만들기
        val messages = createMessages(command)

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

    @Transactional
    override fun archive(
        chats: List<Chat>,
        member: Member,
        subject: Subject,
    ): Long {
        // validate chats and member
        val chatsWrapper = Chats(chats)
        require(chatsWrapper.hasAnswer()) { throw NoAnswerException() }

        // remove all chats
        chatRemover.removeAll(chats)

        // add archive and contents
        val archive = chatArchiveAppender.addArchive(subject, member)
        chatArchiveAppender.addContents(archive, chats.map { it.content })

        return archive.id
    }

    private fun createMessages(command: CreateSubjectAnswerCommand): List<Message> =
        when (command.chats.isEmpty()) {
            true ->
                GrantRoleMessageFactory.create(
                    question = command.subject.question,
                    answer = command.answer,
                )

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
        scoreRegex
            .find(nextQuestion)
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0

    private fun validateNotUseAllAnswers(chats: List<Chat>) {
        val chatsWrapper = Chats(chats)
        require(!chatsWrapper.useAllAnswers()) { throw AllAnswersUsedException() }
    }
}
