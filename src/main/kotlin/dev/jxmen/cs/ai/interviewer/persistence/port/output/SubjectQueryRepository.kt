package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>

    fun findByIdOrNull(id: Long): Subject?

    fun findWithMember(
        member: Member,
        category: SubjectCategory? = null,
    ): List<MemberSubjectResponse>
}
