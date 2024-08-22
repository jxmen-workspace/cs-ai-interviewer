package dev.jxmen.cs.ai.interviewer.persistence

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MemberQueryRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberQueryRepository :
    MemberQueryRepository,
    JpaRepository<Member, Long>
