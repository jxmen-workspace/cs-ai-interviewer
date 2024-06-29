package dev.jxmen.cs.ai.interviewer.domain.subject.exceptions

class SubjectCategoryNotFoundException(
    s: String,
) : IllegalArgumentException("Subject category not found: $s")
