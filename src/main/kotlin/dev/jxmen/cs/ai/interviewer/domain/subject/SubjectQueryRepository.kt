package dev.jxmen.cs.ai.interviewer.domain.subject

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>

    fun findByIdOrNull(id: Long): Subject?
}
