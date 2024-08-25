
package dev.jxmen.cs.ai.interviewer.application.adapter
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SubjectQueryService(
    private val subjectQueryRepository: SubjectQueryRepository,
) : SubjectQuery {
    // 그냥 조회 - 얘도 usecase?
    // class GetSubjectsByCategoryNameService(
    override fun findByCategory(cateStr: String): List<Subject> {
        val subjectCategory = SubjectCategory.valueOf(cateStr.uppercase())

        return this.subjectQueryRepository.findByCategory(subjectCategory)
    }

    // useCase 만들때 먼저 조회 - persistence adapter
    override fun findWithMember(
        member: Member,
        category: String?,
    ): List<MemberSubjectResponse> {
        val categoryEnum = category?.uppercase()?.let { SubjectCategory.valueOf(it) }

        return subjectQueryRepository.findWithMember(member, categoryEnum)
    }

    // persistence adapter
    override fun findByIdOrThrow(id: Long): Subject = this.subjectQueryRepository.findByIdOrNull(id) ?: throw SubjectNotFoundException(id)
}
