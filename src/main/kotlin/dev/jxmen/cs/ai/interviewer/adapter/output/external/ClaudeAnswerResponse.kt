package dev.jxmen.cs.ai.interviewer.adapter.output.external

import com.fasterxml.jackson.annotation.JsonValue

data class ClaudeAnswerResponse(
    val content: List<ClaudeAnswerContent>,
)

data class ClaudeAnswerContent(
    val type: ClaudeAnswerContentType,
    val text: String,
)

enum class ClaudeAnswerContentType {
    TEXT,
    ;

    @JsonValue
    fun toLower(): String = name.lowercase()
}
