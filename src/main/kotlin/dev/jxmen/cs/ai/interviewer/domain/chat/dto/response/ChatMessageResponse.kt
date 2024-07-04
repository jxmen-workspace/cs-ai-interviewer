package dev.jxmen.cs.ai.interviewer.domain.chat.dto.response

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType

data class ChatMessageResponse(
    val message: String,
    val score: Int?,
    val type: String,
) {
    constructor(
        message: String,
        score: Int?,
        type: ChatType,
    ) : this(
        message = message,
        score = score,
        type = type.name.lowercase(),
    )
}
