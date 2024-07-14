package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatApi(
    @Autowired private val httpSession: HttpSession,
    private val subjectQuery: SubjectQuery,
    private val chatQuery: ChatQuery,
) {
    @Deprecated("Use getMessagesV2 instead")
    @GetMapping("/api/chat/messages")
    fun getMessages(
        @Param("subjectId") subjectId: String,
    ): ResponseEntity<ListDataResponse<ChatMessageResponse>> {
        val userSessionId = httpSession.id

        val subject = subjectQuery.findById(subjectId.toLong())
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

    @GetMapping("/api/v2/chat/messages")
    fun getMessagesV2(
        @Param("subjectId") subjectId: String,
    ): ResponseEntity<ListDataResponse<ChatMessageResponse>> {
        val member = httpSession.getAttribute("member") as Member
        val subject = subjectQuery.findById(subjectId.toLong())

        val messages =
            chatQuery.findBySubjectAndMember(
                subject = subject,
                member = member,
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
