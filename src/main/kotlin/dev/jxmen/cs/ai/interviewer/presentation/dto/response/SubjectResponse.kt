package dev.jxmen.cs.ai.interviewer.presentation.dto.response

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory

data class SubjectResponse(
    val id: Long, // String?
    val title: String,
    val category: SubjectCategory,
)
