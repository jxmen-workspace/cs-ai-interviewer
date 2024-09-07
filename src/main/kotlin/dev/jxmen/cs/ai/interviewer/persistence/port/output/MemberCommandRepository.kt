package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember

interface MemberCommandRepository {
    fun save(jpaMember: JpaMember): JpaMember
}
