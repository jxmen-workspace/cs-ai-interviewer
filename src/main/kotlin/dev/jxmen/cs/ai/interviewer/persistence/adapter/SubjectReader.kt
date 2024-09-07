package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.persistence.mapper.SubjectMapper
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectQueryRepository
import org.springframework.stereotype.Component

@Component
class SubjectReader(
    private val subjectQueryRepository: SubjectQueryRepository,
    private val subjectMapper: SubjectMapper,
) {
    fun findById(subjectId: Long): Subject {
        val jpaSubject = subjectQueryRepository.findByIdOrNull(subjectId) ?: throw SubjectNotFoundException(subjectId)

        return subjectMapper.toDomain(jpaSubject)
    }
}
