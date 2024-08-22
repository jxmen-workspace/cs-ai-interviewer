package dev.jxmen.cs.ai.interviewer.adapter.input.dto.request

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory

data class MemberSubjectResponse(
    val id: Long,
    val title: String,
    val category: SubjectCategory,
    val maxScore: Int? = null,
)
