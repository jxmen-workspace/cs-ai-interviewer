package dev.jxmen.cs.ai.interviewer.domain.chat.api
import dev.jxmen.cs.ai.interviewer.domain.chat.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatQuery
import dev.jxmen.cs.ai.interviewer.domain.subject.service.port.SubjectQuery
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import jakarta.servlet.http.HttpSession
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatApi(
    private val httpSession: HttpSession,
    private val subjectQuery: SubjectQuery,
    private val chatQuery: ChatQuery,
) {
    @GetMapping("/messages")
    fun getMessages(
        @Param("subjectId") subjectId: String,
    ): ResponseEntity<ListDataResponse<ChatMessageResponse>> {
        val userSessionId = httpSession.id
        val subject = subjectQuery.getSubjectById(subjectId.toLong())

        val messages =
            chatQuery.findBySubjectAndUserSessionId(
                subject = subject,
                userSessionId = userSessionId,
            )

        return ResponseEntity.ok(
            ListDataResponse(
                messages.map {
                    ChatMessageResponse(
                        message = it.message,
                        score = it.score,
                        type = it.chatType,
                    )
                },
            ),
        )
    }
}
