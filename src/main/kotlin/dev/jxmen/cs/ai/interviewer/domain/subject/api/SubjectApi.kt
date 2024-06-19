package dev.jxmen.cs.ai.interviewer.domain.subject.api

import dev.jxmen.cs.ai.interviewer.domain.subject.dto.SubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.service.SubjectService
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subjects")
class SubjectApi(
    private val subjectService: SubjectService,
) {
    @GetMapping
    fun getSubjects(
        @RequestParam("category") cateStr: String,
    ): ResponseEntity<ListDataResponse<SubjectResponse>> {
        val response =
            ListDataResponse(
                subjectService.getSubjectsByCategory(cateStr).map {
                    SubjectResponse(
                        title = it.title,
                        question = it.question,
                        category = it.category,
                    )
                },
            )

        return ResponseEntity.ok(response)
    }
}
