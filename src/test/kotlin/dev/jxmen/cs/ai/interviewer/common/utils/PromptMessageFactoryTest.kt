package dev.jxmen.cs.ai.interviewer.common.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
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

            val promptMessageFactory = PromptMessageFactory()

            context("create") {
                it("이전 채팅이 없을 때 권한 부여, 질문에 대한 답변, 사용자 답변 메시지 목록을 반환해야 합니다") {
                    // given
                    val answer: String = fixtureMonkey.giveMeOne()
                    val subject: Subject = fixtureMonkey.giveMeOne()

                    // when
                    val result = promptMessageFactory.create(answer, Chats(), subject)

                    // then
                    result shouldHaveSize 3
                    result[0] shouldBe UserMessage(PromptMessageFactory.grantInterviewerRoleMessage)
                    result[1] shouldBe AssistantMessage(promptMessageFactory.getAiAnswerContentFromQuestion(subject.question))
                    result[2] shouldBe UserMessage(answer)
                }

                it("이전 채팅이 있을 때, 첫 채팅을 잘라낸 메시지 목록을 반환해야 합니다") {
                    // given
                    val answer: String = fixtureMonkey.giveMeOne()
                    val subject: Subject = fixtureMonkey.giveMeOne()
                    val chats =
                        Chats(listOf(createQuestionChat(fixtureMonkey), createAnswerChat(fixtureMonkey), createQuestionChat(fixtureMonkey)))

                    // when
                    val result = promptMessageFactory.create(answer, chats, subject)

                    // then
                    result shouldHaveSize 5
                    result[0] shouldBe UserMessage(PromptMessageFactory.grantInterviewerRoleMessage)
                    result[1] shouldBe AssistantMessage(promptMessageFactory.getAiAnswerContentFromQuestion(subject.question))
                    result[2] shouldBe UserMessage(chats.chats[1].message)
                    result[3] shouldBe AssistantMessage(chats.chats[2].message)
                    result[4] shouldBe UserMessage(answer)
                }
            }
        }
    })

private fun createAnswerChat(fixtureMonkey: FixtureMonkey): Chat =
    fixtureMonkey
        .giveMeBuilder<Chat>()
        .setExp(Chat::type, ChatType.ANSWER)
        .sample()

private fun createQuestionChat(fixtureMonkey: FixtureMonkey): Chat =
    fixtureMonkey
        .giveMeBuilder<Chat>()
        .setExp(Chat::type, ChatType.QUESTION)
        .sample()
