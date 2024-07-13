package dev.jxmen.cs.ai.interviewer.domain.member

import java.util.Optional

interface MemberQueryRepository {
    fun findByEmail(email: String): Optional<Member>
}
