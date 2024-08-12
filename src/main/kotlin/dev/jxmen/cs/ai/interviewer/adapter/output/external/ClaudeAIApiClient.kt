package dev.jxmen.cs.ai.interviewer.adapter.output.external

import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.application.port.output.dto.AiApiAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
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
