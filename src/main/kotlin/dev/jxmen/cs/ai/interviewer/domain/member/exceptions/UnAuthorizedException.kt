package dev.jxmen.cs.ai.interviewer.domain.member.exceptions

import dev.jxmen.cs.ai.interviewer.global.enum.ErrorType

class UnAuthorizedException(
    val errorType: ErrorType,
) : RuntimeException()
