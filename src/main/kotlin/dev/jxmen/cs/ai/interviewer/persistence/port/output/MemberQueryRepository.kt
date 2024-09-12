package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberQueryRepository : JpaRepository<JpaMember, Long> {
    fun findByEmail(email: String): Optional<JpaMember>
}
