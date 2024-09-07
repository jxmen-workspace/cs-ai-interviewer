package dev.jxmen.cs.ai.interviewer.domain.subject

import java.time.LocalDateTime

data class Subject(
    val id: Long,
    val title: String,
    val question: String,
    val category: SubjectCategory,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
