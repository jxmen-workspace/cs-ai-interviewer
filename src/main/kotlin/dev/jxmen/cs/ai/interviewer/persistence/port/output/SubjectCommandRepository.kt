package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectCommandRepository : JpaRepository<JpaSubject, Long>
