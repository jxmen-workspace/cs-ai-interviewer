package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SubjectQueryService(
    private val subjectQueryRepository: SubjectQueryRepository,
) : SubjectQuery {
    override fun findByCategory(cateStr: String): List<Subject> {
        val subjectCategory = SubjectCategory.valueOf(cateStr.uppercase())

        return this.subjectQueryRepository.findByCategory(subjectCategory)
    }

    override fun findWithMember(
        member: Member,
        category: String?,
    ): List<MemberSubjectResponse> {
        val categoryEnum = category?.uppercase()?.let { SubjectCategory.valueOf(it) }

        return subjectQueryRepository.findWithMember(member, categoryEnum)
    }

    override fun findByIdOrThrow(id: Long): Subject = this.subjectQueryRepository.findByIdOrNull(id) ?: throw SubjectNotFoundException(id)
}
