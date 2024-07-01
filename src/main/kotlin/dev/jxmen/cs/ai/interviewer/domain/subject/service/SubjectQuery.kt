package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectQuery {
    fun getSubjectsByCategory(cateStr: String): List<Subject>

    fun getSubjectByCategory(id: Long): Subject
}
