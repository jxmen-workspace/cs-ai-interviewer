package dev.jxmen.cs.ai.interviewer.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

data class ClaudeAnswerReqeust(
    val model: String = ClaudeModel.SONNET_3_5.toString(),
    @JsonProperty("max_tokens")
    val maxTokens: Int = 300,
    val messages: List<ClaudeMessage> = emptyList(),
) {
    companion object {
        fun create(
            subject: Subject,
            chats: List<Chat>,
            answer: String,
        ): ClaudeAnswerReqeust {
            val baseMessages =
                listOf(
                    ClaudeMessage(
                        role = ClaudeMessageRole.USER,
                        content = GrantRoleMessageFactory.grantInterviewerRoleMessage,
                    ),
                    ClaudeMessage(
                        role = ClaudeMessageRole.ASSISTANT,
                        content = GrantRoleMessageFactory.getAiAnswerContentFromQuestion(subject.question),
                    ),
                )

            val beforeMessages =
                chats.map {
                    if (it.isAnswer()) {
                        ClaudeMessage(
                            role = ClaudeMessageRole.USER,
                            content = it.content.message,
                        )
                    } else {
                        ClaudeMessage(
                            role = ClaudeMessageRole.ASSISTANT,
                            content = it.content.message,
                        )
                    }
                }
            val answerMessage =
                ClaudeMessage(
                    role = ClaudeMessageRole.USER,
                    content = answer,
                )
            return ClaudeAnswerReqeust(
                messages = baseMessages + beforeMessages + answerMessage,
            )
        }
    }
}
