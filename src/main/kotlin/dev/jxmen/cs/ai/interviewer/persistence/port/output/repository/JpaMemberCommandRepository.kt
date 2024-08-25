package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberCommandRepository :
    MemberCommandRepository,
    JpaRepository<Member, Long>
