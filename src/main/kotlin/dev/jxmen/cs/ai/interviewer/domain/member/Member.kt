package dev.jxmen.cs.ai.interviewer.domain.member

import java.time.LocalDateTime

class Member(
    val id: Long,
    val name: String,
    val email: String,
    val loginType: MemberLoginType,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
