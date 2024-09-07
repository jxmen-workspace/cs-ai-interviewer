package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse

interface SubjectQuery {
    fun findByCategory(category: String): List<JpaSubject>

    fun findWithMember(
        jpaMember: JpaMember,
        category: String? = null,
    ): List<MemberSubjectResponse>

    /**
     * NOTE: 이 메서드는 일관된 API 포맷을 지키기 위해 추가되었습니다.
     *
     * 이 메서드에서 null을 반환했을 때 발생시키는 예외는 일관된 API 포맷을 지키기 위한 것입니다.
     */
    fun findByIdOrThrow(id: Long): JpaSubject
}
