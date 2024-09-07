package dev.jxmen.cs.ai.interviewer.persistence.mapper

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import org.springframework.stereotype.Component

@Component
class SubjectMapper {
    fun toDomain(jpaSubject: JpaSubject): Subject =
        Subject(
            id = jpaSubject.id,
            title = jpaSubject.title,
            question = jpaSubject.question,
            category = jpaSubject.category,
            createdAt = jpaSubject.createdAt,
            updatedAt = jpaSubject.updatedAt,
        )

    fun toJpaEntity(subject: Subject): JpaSubject =
        JpaSubject(
            id = subject.id,
            title = subject.title,
            question = subject.question,
            category = subject.category,
            createdAt = subject.createdAt,
            updatedAt = subject.updatedAt,
        )
}
