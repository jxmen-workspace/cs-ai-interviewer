package dev.jxmen.cs.ai.interviewer.presentation.dto.response

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory

data class SubjectDetailResponse(
    val id: Long,
    val category: SubjectCategory,
    val title: String,
    val question: String,
)
