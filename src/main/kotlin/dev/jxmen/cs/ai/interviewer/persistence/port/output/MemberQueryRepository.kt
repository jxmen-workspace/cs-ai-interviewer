package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import java.util.Optional

interface MemberQueryRepository {
    fun findByEmail(email: String): Optional<JpaMember>
}
