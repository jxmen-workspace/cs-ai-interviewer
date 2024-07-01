package dev.jxmen.cs.ai.interviewer.domain.subject.api

import dev.jxmen.cs.ai.interviewer.domain.subject.dto.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.SubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.service.SubjectQuery
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subjects")
class SubjectApi(
    private val subjectQuery: SubjectQuery,
) {
    @GetMapping
    fun getSubjects(
        @RequestParam("category") cateStr: String,
    ): ResponseEntity<ListDataResponse<SubjectResponse>> {
        val response =
            ListDataResponse(
                subjectQuery.getSubjectsByCategory(cateStr).map {
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
        val subject = subjectQuery.getSubjectByCategory(id.toLong())

        return ResponseEntity.ok(
            SubjectDetailResponse(
                id = subject.id,
                category = subject.category,
                title = subject.title,
                question = subject.question,
            ),
        )
    }
}
