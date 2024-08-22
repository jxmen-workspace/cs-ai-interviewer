package dev.jxmen.cs.ai.interviewer.common.exceptions

import dev.jxmen.cs.ai.interviewer.common.enum.ErrorType

class ServerError(
    val errorType: ErrorType,
) : RuntimeException()
