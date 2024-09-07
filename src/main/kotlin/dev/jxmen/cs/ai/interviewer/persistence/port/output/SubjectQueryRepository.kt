package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<JpaSubject>

    fun findByIdOrNull(id: Long): JpaSubject?

    fun findWithMember(
        jpaMember: JpaMember,
        category: SubjectCategory? = null,
    ): List<MemberSubjectResponse>
}
