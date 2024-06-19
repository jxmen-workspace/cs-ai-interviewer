package dev.jxmen.cs.ai.interviewer.infra

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaSubjectRepository : JpaRepository<Subject, Long>, SubjectRepository
