package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberCommandRepository :
    MemberCommandRepository,
    JpaRepository<JpaMember, Long>
