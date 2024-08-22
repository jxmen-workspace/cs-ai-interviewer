package dev.jxmen.cs.ai.interviewer.persistence

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MemberCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberCommandRepository :
    MemberCommandRepository,
    JpaRepository<Member, Long>
