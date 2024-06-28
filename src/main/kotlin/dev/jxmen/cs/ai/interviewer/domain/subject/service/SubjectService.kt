package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
) : SubjectUseCase {
    @Transactional(readOnly = true)
    override fun getSubjectsByCategory(cateStr: String): List<Subject> {
        val subjectCategory = SubjectCategory.valueOf(cateStr.uppercase())

        return this.subjectRepository.findByCategory(subjectCategory)
    }
}
