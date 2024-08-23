package dev.jxmen.cs.ai.interviewer.presentation

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.common.RequireLoginApi
import dev.jxmen.cs.ai.interviewer.common.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.common.dto.ListDataResponse
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.presentation.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.presentation.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.presentation.dto.response.SubjectResponse
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.net.URI

@RestController
class SubjectApi(
    private val subjectQuery: SubjectQuery,
    private val chatQuery: ChatQuery,
    private val memberChatUseCase: MemberChatUseCase,
) {
    /**
     * 주제 목록 조회
     */
    @GetMapping("/api/v1/subjects")
    fun getSubjects(
        @RequestParam("category") cateStr: String,
    ): ResponseEntity<ApiResponse<List<SubjectResponse>>> {
        val subjects = subjectQuery.findByCategory(cateStr)

        val response =
            ListDataResponse(
                subjects.map {
                    SubjectResponse(id = it.id, title = it.title, category = it.category)
                },
            )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 특정 주제 조회
     */
    @GetMapping("/api/v1/subjects/{id}")
    fun getSubject(
        @PathVariable("id") id: String,
    ): ResponseEntity<ApiResponse<SubjectDetailResponse>> {
        val subject = subjectQuery.findByIdOrThrow(id.toLong())

        return ResponseEntity.ok(
            ApiResponse.success(
                SubjectDetailResponse(
                    id = subject.id,
                    category = subject.category,
                    title = subject.title,
                    question = subject.question,
                ),
            ),
        )
    }

    /**
     * 특정 주제 채팅 내역 조회
     */
    @GetMapping("/api/v1/subjects/{subjectId}/chats")
    @RequireLoginApi
    fun getChats(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
    ): ResponseEntity<ApiResponse<List<ChatMessageResponse>>> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val messages = chatQuery.findBySubjectAndMember(subject = subject, member = member)

        val list =
            messages.map {
                ChatMessageResponse(
                    message = it.content.message,
                    score = it.content.score,
                    type = it.content.chatType,
                    createdAt = it.createdAt,
                )
            }

        return ResponseEntity.ok(
            ApiResponse.success(
                ListDataResponse(list),
            ),
        )
    }

    /**
     * 답변 등록 (비동기)
     */
    @GetMapping("/api/v5/subjects/{subjectId}/answer")
    @RequireLoginApi
    fun answerSubjectV5Async(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
        @Param("message") message: String,
    ): Flux<ChatResponse> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val command =
            CreateSubjectAnswerCommand(
                subject = subject,
                answer = message,
                member = member,
                chats = chats,
            )

        return memberChatUseCase.answerAsync(command)
    }

    /**
     * 로그인한 회원의 주제 목록 조회 (회원 관련 정보 포함 - ex)채팅 최대 점수)
     */
    @GetMapping("/api/v1/subjects/my")
    @RequireLoginApi
    fun getMemberSubjects(
        member: Member,
        @Param("category") category: String?,
    ): ResponseEntity<ApiResponse<List<MemberSubjectResponse>>> {
        val response = ListDataResponse(subjectQuery.findWithMember(member, category))

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 채팅 내역 아카이브
     */
    @PostMapping("/api/v2/subjects/{subjectId}/chats/archive")
    @RequireLoginApi
    fun deleteMessages(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val id = memberChatUseCase.archive(chats, member, subject)

        return ResponseEntity
            .created(URI("/api/v1/chat/archives/$id"))
            .body(ApiResponse.success())
    }
}
