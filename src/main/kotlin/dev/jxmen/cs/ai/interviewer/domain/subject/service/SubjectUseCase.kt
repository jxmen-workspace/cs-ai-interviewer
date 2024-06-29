package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectUseCase {
    fun getSubjectsByCategory(cateStr: String): List<Subject>

    fun getSubjectByCategory(id: Long): Subject
}
