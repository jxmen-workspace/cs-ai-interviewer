package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubjectQueryService(
    private val subjectQueryRepository: SubjectQueryRepository,
) : SubjectQuery {
    @Transactional(readOnly = true)
    override fun findBySubject(cateStr: String): List<Subject> {
        val subjectCategory = SubjectCategory.valueOf(cateStr.uppercase())

        return this.subjectQueryRepository.findByCategory(subjectCategory)
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): Subject = this.subjectQueryRepository.findByIdOrNull(id) ?: throw SubjectNotFoundException(id)
}
