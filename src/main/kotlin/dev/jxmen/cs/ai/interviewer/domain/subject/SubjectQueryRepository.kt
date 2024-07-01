package dev.jxmen.cs.ai.interviewer.domain.subject

import java.util.Optional

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>

    fun findById(id: Long): Optional<Subject>
}
