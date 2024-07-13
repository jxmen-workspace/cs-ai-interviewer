package dev.jxmen.cs.ai.interviewer.domain.member

interface MemberCommandRepository {
    fun save(member: Member): Member
}
