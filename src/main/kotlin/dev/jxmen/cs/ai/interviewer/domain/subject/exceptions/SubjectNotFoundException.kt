package dev.jxmen.cs.ai.interviewer.domain.subject.exceptions

class SubjectNotFoundException(
    id: Long,
) : RuntimeException("Subject not found by id: $id")
