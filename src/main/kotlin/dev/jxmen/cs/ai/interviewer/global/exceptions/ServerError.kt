package dev.jxmen.cs.ai.interviewer.global.exceptions

import dev.jxmen.cs.ai.interviewer.global.enum.ErrorType

class ServerError(
    val errorType: ErrorType,
) : RuntimeException()
