package dev.jxmen.cs.ai.interviewer.presentation.dto.response

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChatMessageResponse(
    val message: String,
    val score: Int?,
    val type: String,
    val createdAt: String?,
) {
    constructor(
        message: String,
        score: Int?,
        type: ChatType,
        createdAt: LocalDateTime?,
    ) : this(
        message = message,
        score = score,
        type = type.name.lowercase(),
        createdAt =
            when (type) {
                ChatType.QUESTION -> null
                ChatType.ANSWER -> createdAt?.format(DateTimeFormatter.ISO_DATE_TIME)
            },
    )
}
