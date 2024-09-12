package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import org.springframework.data.jpa.repository.JpaRepository

interface MemberCommandRepository : JpaRepository<JpaMember, Long>
