package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject

interface SubjectQuery {
    fun findBySubject(cateStr: String): List<Subject>

    fun findById(id: Long): Subject

    fun findWithMember(
        member: Member,
        category: String? = null,
    ): List<MemberSubjectResponse>
}
