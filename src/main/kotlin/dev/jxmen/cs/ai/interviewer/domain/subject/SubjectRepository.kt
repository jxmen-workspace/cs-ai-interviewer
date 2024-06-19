package dev.jxmen.cs.ai.interviewer.domain.subject

interface SubjectRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>
}
