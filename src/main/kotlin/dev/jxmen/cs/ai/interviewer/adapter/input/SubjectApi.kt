package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.SubjectAnswerRequest
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2
import dev.jxmen.cs.ai.interviewer.domain.member.Member
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

@RestController
class SubjectApi(
    private val subjectQuery: SubjectQuery,
    private val subjectUseCase: SubjectUseCase,
) {
    @GetMapping("/api/subjects")
    fun getSubjects(
        @RequestParam("category") cateStr: String,
    ): ResponseEntity<ListDataResponse<SubjectResponse>> {
        val response =
            ListDataResponse(
                subjectQuery.findBySubject(cateStr).map {
                    SubjectResponse(
                        id = it.id,
                        title = it.title,
                        category = it.category,
                    )
                },
            )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/subjects/{id}")
    fun getSubject(
        @PathVariable("id") id: String,
    ): ResponseEntity<SubjectDetailResponse> {
        val subject = subjectQuery.findById(id.toLong())

        return ResponseEntity.ok(
            SubjectDetailResponse(
                id = subject.id,
                category = subject.category,
                title = subject.title,
                question = subject.question,
            ),
        )
    }

    @PostMapping("/api/v2/subjects/{subjectId}/answer")
    fun answerSubjectV2(
        member: Member,
        @PathVariable("subjectId") subjectId: String,
        @RequestBody @Valid req: SubjectAnswerRequest,
    ): ResponseEntity<SubjectAnswerResponse> {
        val subject = subjectQuery.findById(subjectId.toLong())

        val res =
            subjectUseCase.answerV2(
                CreateSubjectAnswerCommandV2(
                    subject = subject,
                    answer = req.answer,
                    member = member,
                ),
            )

        return ResponseEntity.status(201).body(res)
    }

    @GetMapping("/api/v1/subjects/member")
    fun getMemberSubjects(
        member: Member,
        @Param("category") category: String?,
    ): ResponseEntity<ListDataResponse<MemberSubjectResponse>> {
        val response = ListDataResponse(subjectQuery.findWithMember(member, category))

        return ResponseEntity.ok(response)
    }
}
