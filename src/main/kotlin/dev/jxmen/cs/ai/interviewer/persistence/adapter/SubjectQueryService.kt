
package dev.jxmen.cs.ai.interviewer.persistence.adapter
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectQueryRepository
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
    override fun findByCategory(category: String): List<JpaSubject> {
        val subjectCategory = SubjectCategory.valueOf(category.uppercase())

        return this.subjectQueryRepository.findByCategory(subjectCategory)
    }

    // useCase 만들때 먼저 조회 - persistence adapter
    override fun findWithMember(
        jpaMember: JpaMember,
        category: String?,
    ): List<MemberSubjectResponse> {
        val categoryEnum = category?.uppercase()?.let { SubjectCategory.valueOf(it) }

        return subjectQueryRepository.findWithMember(jpaMember, categoryEnum)
    }

    // persistence adapter
    override fun findByIdOrThrow(id: Long): JpaSubject =
        this.subjectQueryRepository.findByIdOrNull(id) ?: throw SubjectNotFoundException(id)
}
