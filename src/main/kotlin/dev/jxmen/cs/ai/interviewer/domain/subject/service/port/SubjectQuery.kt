package dev.jxmen.cs.ai.interviewer.domain.subject.service.port

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectQuery {
    fun getSubjectsByCategory(cateStr: String): List<Subject>

    fun getSubjectById(id: Long): Subject
}
