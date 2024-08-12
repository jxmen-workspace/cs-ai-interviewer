package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatApi(
    private val subjectQuery: SubjectQuery,
    private val chatQuery: ChatQuery,
) {
    @GetMapping("/api/v2/chat/messages")
    fun getMessagesV2(
        member: Member,
        @Param("subjectId") subjectId: String,
    ): ResponseEntity<ListDataResponse<ChatMessageResponse>> {
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
