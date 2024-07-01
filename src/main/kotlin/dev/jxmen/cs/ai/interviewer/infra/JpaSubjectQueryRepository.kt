package dev.jxmen.cs.ai.interviewer.infra

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import org.springframework.data.jpa.repository.JpaRepository

@SuppressWarnings("unused")
interface JpaSubjectQueryRepository :
    SubjectQueryRepository,
    JpaRepository<Subject, Long>
