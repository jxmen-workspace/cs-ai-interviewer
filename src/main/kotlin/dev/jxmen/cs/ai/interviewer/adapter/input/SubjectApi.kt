package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.SubjectAnswerRequest
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subjects")
class SubjectApi(
    private val subjectQuery: SubjectQuery,
    private val subjectUseCase: SubjectUseCase,
) {
    @GetMapping
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

    @GetMapping("/{id}")
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

    @PostMapping("/{id}/answer")
    fun answerSubject(
        @PathVariable("id") id: String,
        @RequestBody @Valid req: SubjectAnswerRequest,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<SubjectAnswerResponse> {
        val subject = subjectQuery.findById(id.toLong())
        val res =
            subjectUseCase.answer(
                CreateSubjectAnswerCommand(
                    subject = subject,
                    answer = req.answer,
                    userSessionId = httpServletRequest.session.id,
                ),
            )

        return ResponseEntity.status(201).body(res)
    }
}
