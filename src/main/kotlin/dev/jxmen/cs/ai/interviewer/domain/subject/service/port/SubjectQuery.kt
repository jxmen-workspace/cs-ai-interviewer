package dev.jxmen.cs.ai.interviewer.domain.subject.service.port

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectQuery {
    fun findBySubject(cateStr: String): List<Subject>

    fun findById(id: Long): Subject
}
