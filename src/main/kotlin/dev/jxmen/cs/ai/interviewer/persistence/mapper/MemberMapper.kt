package dev.jxmen.cs.ai.interviewer.persistence.mapper

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import org.springframework.stereotype.Component

@Component
class MemberMapper {
    fun toJpaEntity(member: Member): JpaMember =
        JpaMember(
            id = member.id,
            name = member.name,
            email = member.email,
            loginType = member.loginType,
            createdAt = member.createdAt,
            updatedAt = member.updatedAt,
        )

    fun toDomain(jpaMember: JpaMember): Member =
        Member(
            id = jpaMember.id,
            name = jpaMember.name,
            email = jpaMember.email,
            loginType = jpaMember.loginType,
            createdAt = jpaMember.createdAt,
            updatedAt = jpaMember.updatedAt,
        )
}
