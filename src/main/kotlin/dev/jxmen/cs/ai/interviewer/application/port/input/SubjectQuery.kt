package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectQuery {
    fun findByCategory(cateStr: String): List<Subject>

    fun findWithMember(
        member: Member,
        category: String? = null,
    ): List<MemberSubjectResponse>

    fun findByIdOrThrow(id: Long): Subject

    /**
     * NOTE: 이 메서드는 일관된 API 포맷을 지키기 위해 추가되었습니다.
     *
     * 이 메서드에서 null을 반환했을 때 발생시키는 예외는 일관된 API 포맷을 지키기 위한 것입니다.
     */
    fun findByIdOrThrowV2(id: Long): Subject
}
