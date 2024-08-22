package dev.jxmen.cs.ai.interviewer.common.exceptions

import dev.jxmen.cs.ai.interviewer.common.enum.ErrorType

class UnAuthorizedException(
    val errorType: ErrorType,
) : RuntimeException()
