package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.member.Member

interface SubjectQueryRepository {
    fun findByCategory(category: SubjectCategory): List<Subject>

    fun findByIdOrNull(id: Long): Subject?

    fun findWithMember(
        member: Member,
        valueOf: SubjectCategory?,
    ): List<MemberSubjectResponse>
}
