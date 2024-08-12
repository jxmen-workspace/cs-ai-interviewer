package dev.jxmen.cs.ai.interviewer.adapter.output.external

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.application.port.output.dto.AiApiAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ClaudeAIApiClient(
    @Value("\${claude.api-key}")
    private val claudeApiKey: String,
) : AIApiClient {
    companion object {
        private const val BASE_URL = "https://api.anthropic.com/v1"

        private val client = RestClient.create()

        private val scoreRegex = "답변에 대한 점수: (\\d+)점".toRegex()
    }

    override fun requestAnswer(
        subject: Subject,
        answer: String,
        chats: List<Chat>,
    ): AiApiAnswerResponse {
        val response =
            client
                .post()
                .uri("$BASE_URL/messages")
                .header("Content-Type", "application/json")
                .header("anthropic-version", "2023-06-01")
                .header("x-api-key", claudeApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ClaudeAnswerReqeust.create(
                        subject = subject,
                        chats = chats,
                        answer = answer,
                    ),
                ).retrieve()
                .body(ClaudeAnswerResponse::class.java)

        return AiApiAnswerResponse(
            nextQuestion =
                response
                    ?.content
                    ?.get(0)
                    ?.text
                    .toString(),
            score = parseScoreFrom(response),
        )
    }

    private fun parseScoreFrom(response: ClaudeAnswerResponse?): Int {
        val scoreMatchResult =
            response
                ?.content
                ?.get(0)
                ?.text
                ?.let { scoreRegex.find(it) }

        return scoreMatchResult
            ?.groups
            ?.get(1)
            ?.value
            ?.toInt() ?: 0
    }
}

enum class ClaudeMessageRole {
    USER,
    ASSISTANT,
    ;

    @JsonValue
    fun toLower(): String = name.lowercase()
}

data class ClaudeMessage(
    val role: ClaudeMessageRole,
    val content: String,
)

data class ClaudeAnswerReqeust(
    val model: String = "claude-3-5-sonnet-20240620",
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
            val grantInterviewerContent =
                """
                당신은 이제부터 Computer Science에 대해 질문하는 면접관이다. 
                당신이 안내한 질문에 대해 내가 답변을 하면, 그에 대한 점수를 그동안 해왔던 답변도 포함하여 10점 단위로 평가 및 이유와 공부할 수 있는 키워드, 꼬리 질문을 제시해주길 바란다.
                
                점수에 대한 기준은 다음과 같다.
                (0: 기초 부족 혹은 적절한 대답이 아님, 10~30: 기초 수준만 아는 상태, 40~60: 어느 정도 알고 있음, 70~90: 어느 정도 깊게 알고 있음, 100: 매우 깊게 알고 있음)
                
                답변 형식은 다음과 같다. (매우 중요: 내가 어떠한 답변을 해도 답변 내용에 따라 아래의 형식을 줄바꿈하여 맞추고, 면접관은 당신은 직접적인 정답을 제공해서는 안된다.)
                
                답변에 대한 점수: nn점
                이유: ~~에 대한 설명은 부족합니다 / ~~에 대한 설명은 틀렸습니다 / ~~에 대해 더 깊게 공부하세요.
                공부할 수 있는 키워드: ~~ / ~~ / ~~
                꼬리 질문: ~~에 대해 더 깊게 설명해보세요.
                """.trimMargin()

            val aiAnswerAndQuestionContent =
                """
                네, 알겠습니다. 제공해주신 형식에 맞추어 답변하는 면접관 역할을 수행하고, 질문에 대한 답은 제공하지 않겠습니다. 
                제가 면접관으로 질문드릴 내용은 다음과 같습니다.
                
                질문: ${subject.question}
                """.trimIndent()

            val baseMessages =
                listOf(
                    ClaudeMessage(
                        role = ClaudeMessageRole.USER,
                        content = grantInterviewerContent,
                    ),
                    ClaudeMessage(
                        role = ClaudeMessageRole.ASSISTANT,
                        content = aiAnswerAndQuestionContent,
                    ),
                )

            val beforeMessages =
                chats.map {
                    if (it.chatType == ChatType.ANSWER) {
                        ClaudeMessage(
                            role = ClaudeMessageRole.USER,
                            content = it.message,
                        )
                    } else {
                        ClaudeMessage(
                            role = ClaudeMessageRole.ASSISTANT,
                            content = it.message,
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

enum class ClaudeAnswerContentType {
    TEXT,
    ;

    @JsonValue
    fun toLower(): String = name.lowercase()
}

data class ClaudeAnswerContent(
    val type: ClaudeAnswerContentType,
    val text: String,
)

data class ClaudeAnswerResponse(
    val content: List<ClaudeAnswerContent>,
)

// TODO: main function development 테스트로 변경하기 (CI에서는 실행되지 않는 코드 환경 구성)

// fun main() {
//     val aiApiClient =
//         ClaudeAIApiClient(
//             System.getenv("CLAUDE_API_KEY") ?: error("CLAUDE_API_KEY is not set"),
//         )
//     val subject =
//         Subject(
//             id = 1,
//             title = "OSI 7계층",
//             question = "OSI 7계층에 대해 설명하시오.",
//             category = SubjectCategory.NETWORK,
//         )
//     val response =
//         aiApiClient.requestAnswer(
//             subject = subject,
//             answer =
//             """
//                 구성은 물리, 데이터 링크, 네트워크, 전송, 세션, 표현, 응용 계층입니다.
//                 각 계층은 특정 프로토콜을 사용하여 역할을 수행하며, 하위 계층에 의존합니다. 예: HTTP(7계층)는 TCP(4계층)에 의존.
//                 """.trimIndent(),
//             chats =
//             listOf(
//                 Chat(
//                     subject = subject,
//                     chatType = ChatType.ANSWER,
//                     message = "OSI 7계층은 네트워크의 다양한 역할을 레이어로 구분합니다.",
//                     userSessionId = "1",
//                     score = 30,
//                 ),
//                 Chat(
//                     subject = subject,
//                     chatType = ChatType.QUESTION,
//                     message = "더 자세히 설명해주세요.",
//                     userSessionId = "1",
//                 ),
//             ),
//         )
//     println(response)
// }
//
