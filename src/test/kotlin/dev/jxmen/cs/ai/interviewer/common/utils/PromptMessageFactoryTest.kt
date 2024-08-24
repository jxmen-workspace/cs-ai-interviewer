package dev.jxmen.cs.ai.interviewer.common.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.set
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
                    val command =
                        fixtureMonkey
                            .giveMeBuilder<CreateSubjectAnswerCommand>()
                            .set(CreateSubjectAnswerCommand::chats, emptyList<Chat>())
                            .sample()

                    // Arrange
                    val result = PromptMessageFactory.create(command)

                    // Act
                    result shouldHaveSize 3
                    result[0] shouldBe UserMessage(PromptMessageFactory.grantInterviewerRoleMessage)
                    result[1] shouldBe AssistantMessage(PromptMessageFactory.getAiAnswerContentFromQuestion(command.subject.question))
                    result[2] shouldBe UserMessage(command.answer)
                }

                it("이전 채팅이 있을 때, 첫 채팅을 잘라낸 메시지 목록을 반환해야 합니다") {
                    // Arrange
                    val firstQuestionChatContent =
                        fixtureMonkey
                            .giveMeBuilder<ChatContent>()
                            .set(ChatContent::chatType, ChatType.QUESTION)
                            .set(ChatContent::score, null)
                            .sample()
                    val answerChatContent =
                        fixtureMonkey
                            .giveMeBuilder<ChatContent>()
                            .set(ChatContent::chatType, ChatType.ANSWER)
                            .sample()
                    val questionChatContent =
                        fixtureMonkey
                            .giveMeBuilder<ChatContent>()
                            .set(ChatContent::chatType, ChatType.QUESTION)
                            .set(ChatContent::score, null)
                            .sample()

                    val firstQuestionChat = fixtureMonkey.giveMeBuilder<Chat>().set(Chat::content, firstQuestionChatContent).sample()
                    val answerChat = fixtureMonkey.giveMeBuilder<Chat>().set(Chat::content, answerChatContent).sample()
                    val questionChat = fixtureMonkey.giveMeBuilder<Chat>().set(Chat::content, questionChatContent).sample()
                    val command =
                        fixtureMonkey
                            .giveMeBuilder<CreateSubjectAnswerCommand>()
                            .set(CreateSubjectAnswerCommand::chats, listOf(firstQuestionChat, answerChat, questionChat))
                            .sample()

                    // Act
                    val result = PromptMessageFactory.create(command)

                    // Assert
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
