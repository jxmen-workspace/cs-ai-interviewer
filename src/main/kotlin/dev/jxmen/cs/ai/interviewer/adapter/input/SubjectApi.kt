package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.SubjectAnswerRequest
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.ChatMessageResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.global.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import jakarta.validation.Valid
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    @PreAuthorize("permitAll()")
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
    @PreAuthorize("permitAll()")
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
    @PreAuthorize("isAuthenticated()")
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
     * 답변 등록
     */
    @PostMapping("/api/v4/subjects/{subjectId}/answer")
    @PreAuthorize("isAuthenticated()")
    fun answerSubjectV4(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
        @RequestBody @Valid req: SubjectAnswerRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val command =
            CreateSubjectAnswerCommand(
                subject = subject,
                answer = req.answer,
                member = member,
                chats = chats,
            )
        val answerResponse = memberChatUseCase.answer(command)

        return ResponseEntity.status(201).body(
            ApiResponse.success(answerResponse),
        )
    }

    /**
     * 로그인한 회원의 주제 목록 조회 (회원 관련 정보 포함 - ex)채팅 최대 점수)
     */
    @GetMapping("/api/v1/subjects/my")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
