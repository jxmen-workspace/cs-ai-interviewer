package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.domain.member.Member

interface MemberCommandRepository {
    fun save(member: Member): Member
}
