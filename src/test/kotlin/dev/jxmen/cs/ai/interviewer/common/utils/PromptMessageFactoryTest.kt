package dev.jxmen.cs.ai.interviewer.common.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.setExp
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatContent
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage

class PromptMessageFactoryTest :
    DescribeSpec({
        describe("PromptMessageFactory") {
            val fixtureMonkey =
                FixtureMonkey
                    .builder()
                    .apply { plugin(KotlinPlugin()) }
                    .build()

            context("create") {
                it("이전 채팅이 없을 때 권한 부여, 질문에 대한 답변, 사용자 답변 메시지 목록을 반환해야 합니다") {
                    // given
                    val command =
                        fixtureMonkey
                            .giveMeBuilder<CreateSubjectAnswerCommand>()
                            .setExp(CreateSubjectAnswerCommand::chats, emptyList<Chat>())
                            .sample()

                    // when
                    val result = PromptMessageFactory.create(command)

                    // then
                    result shouldHaveSize 3
                    result[0] shouldBe UserMessage(PromptMessageFactory.grantInterviewerRoleMessage)
                    result[1] shouldBe AssistantMessage(PromptMessageFactory.getAiAnswerContentFromQuestion(command.subject.question))
                    result[2] shouldBe UserMessage(command.answer)
                }

                it("이전 채팅이 있을 때, 첫 채팅을 잘라낸 메시지 목록을 반환해야 합니다") {
                    // given
                    val firstQuestionChat = createQuestionChat(fixtureMonkey)
                    val answerChat = createAnswerChat(fixtureMonkey)
                    val questionChat = createQuestionChat(fixtureMonkey)
                    val command =
                        fixtureMonkey
                            .giveMeBuilder<CreateSubjectAnswerCommand>()
                            .setExp(CreateSubjectAnswerCommand::chats, listOf(firstQuestionChat, answerChat, questionChat))
                            .sample()

                    // when
                    val result = PromptMessageFactory.create(command)

                    // then
                    result shouldHaveSize 5
                    result[0] shouldBe UserMessage(PromptMessageFactory.grantInterviewerRoleMessage)
                    result[1] shouldBe AssistantMessage(PromptMessageFactory.getAiAnswerContentFromQuestion(command.subject.question))
                    result[2] shouldBe UserMessage(answerChat.content.message)
                    result[3] shouldBe AssistantMessage(questionChat.content.message)
                    result[4] shouldBe UserMessage(command.answer)
                }
            }
        }
    })

private fun createAnswerChat(fixtureMonkey: FixtureMonkey): Chat =
    fixtureMonkey
        .giveMeBuilder<Chat>()
        .setExp(Chat::content into ChatContent::chatType, ChatType.ANSWER)
        .setPostCondition { it.content.score in 0..100 }
        .sample()

private fun createQuestionChat(fixtureMonkey: FixtureMonkey): Chat =
    fixtureMonkey
        .giveMeBuilder<Chat>()
        .setExp(Chat::content into ChatContent::chatType, ChatType.QUESTION)
        .setExp(Chat::content into ChatContent::score, null)
        .sample()
