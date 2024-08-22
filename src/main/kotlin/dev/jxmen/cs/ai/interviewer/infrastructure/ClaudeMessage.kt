package dev.jxmen.cs.ai.interviewer.infrastructure

import com.fasterxml.jackson.annotation.JsonValue

data class ClaudeMessage(
    val role: ClaudeMessageRole,
    val content: String,
)

enum class ClaudeMessageRole {
    USER,
    ASSISTANT,
    ;

    @JsonValue
    fun toLower(): String = name.lowercase()
}
