package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.global.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ChatApi(
    private val subjectQuery: SubjectQuery,
    private val chatQuery: ChatQuery,
    private val memberChatUseCase: MemberChatUseCase,
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
                        message = it.content.message,
                        score = it.content.score,
                        type = it.content.chatType,
                    )
                },
            ),
        )
    }

    /**
     * 채팅 내역 초기화
     */
    @PostMapping("/api/v1/chat/archive/{subjectId}")
    fun deleteMessages(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val subject = subjectQuery.findByIdV2(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val id = memberChatUseCase.archive(chats, member, subject)

        return ResponseEntity
            .created(URI("/api/v1/chat/archives/$id"))
            .body(ApiResponse.success())
    }
}
