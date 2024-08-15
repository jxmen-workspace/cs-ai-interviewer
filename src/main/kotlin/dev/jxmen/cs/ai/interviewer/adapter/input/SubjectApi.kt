package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.SubjectAnswerRequest
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
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
    @GetMapping("/api/subjects")
    fun getSubjects(
        @RequestParam("category") cateStr: String,
    ): ResponseEntity<ListDataResponse<SubjectResponse>> {
        val subjects = subjectQuery.findByCategory(cateStr)

        val response =
            ListDataResponse(
                subjects.map {
                    SubjectResponse(id = it.id, title = it.title, category = it.category)
                },
            )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/subjects/{id}")
    fun getSubject(
        @PathVariable("id") id: String,
    ): ResponseEntity<SubjectDetailResponse> {
        val subject = subjectQuery.findByIdOrThrow(id.toLong())

        return ResponseEntity.ok(
            SubjectDetailResponse(
                id = subject.id,
                category = subject.category,
                title = subject.title,
                question = subject.question,
            ),
        )
    }

    /**
     * 답변 등록 (V2)
     */
    @Deprecated("V3로 변경 예정")
    @PostMapping("/api/v2/subjects/{subjectId}/answer")
    fun answerSubjectV2(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
        @RequestBody @Valid req: SubjectAnswerRequest,
    ): ResponseEntity<SubjectAnswerResponse> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val command =
            CreateSubjectAnswerCommand(
                subject = subject,
                answer = req.answer,
                member = member,
                chats = chats,
            )
        val answerResponse = memberChatUseCase.answerV2(command)

        return ResponseEntity.status(201).body(answerResponse)
    }

    /**
     * 답변 등록
     */
    @PostMapping("/api/v3/subjects/{subjectId}/answer")
    fun answerSubjectV3(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
        @RequestBody @Valid req: SubjectAnswerRequest,
    ): ResponseEntity<SubjectAnswerResponse> {
        val subject = subjectQuery.findByIdOrThrow(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val command =
            CreateSubjectAnswerCommand(
                subject = subject,
                answer = req.answer,
                member = member,
                chats = chats,
            )
        val answerResponse = memberChatUseCase.answerV3(command)

        return ResponseEntity.status(201).body(answerResponse)
    }

    /**
     * 로그인한 회원의 주제 목록 조회 (회원 관련 정보 포함 - ex)채팅 최대 점수)
     */
    @GetMapping("/api/v1/subjects/member")
    fun getMemberSubjects(
        member: Member,
        @Param("category") category: String?,
    ): ResponseEntity<ListDataResponse<MemberSubjectResponse>> {
        val response = ListDataResponse(subjectQuery.findWithMember(member, category))

        return ResponseEntity.ok(response)
    }

    /**
     * 채팅 내역 아카이브
     */
    @PostMapping("/api/v1/subjects/{subjectId}/chats/archive")
    fun deleteMessages(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val subject = subjectQuery.findByIdOrThrowV2(subjectId.toLong())
        val chats = chatQuery.findBySubjectAndMember(subject, member)

        val id = memberChatUseCase.archive(chats, member, subject)

        return ResponseEntity
            .created(URI("/api/v1/chat/archives/$id"))
            .body(ApiResponse.success())
    }
}
