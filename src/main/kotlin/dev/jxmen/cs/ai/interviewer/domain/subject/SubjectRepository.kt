package dev.jxmen.cs.ai.interviewer.domain.subject

import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long> {
    fun findByCategory(category: SubjectCategory): List<Subject>
}
