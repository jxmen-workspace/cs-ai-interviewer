package dev.jxmen.cs.ai.interviewer.domain.subject.dto

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory

data class SubjectResponse(
    val title: String,
    val question: String,
    val category: SubjectCategory,
)
