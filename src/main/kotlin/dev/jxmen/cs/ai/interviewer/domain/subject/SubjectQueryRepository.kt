package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>

    fun findByIdOrNull(id: Long): Subject?

    fun findWithMember(
        member: Member,
        category: SubjectCategory? = null,
    ): List<MemberSubjectResponse>
}
