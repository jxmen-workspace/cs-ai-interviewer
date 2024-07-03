package dev.jxmen.cs.ai.interviewer.external.port

data class AiApiAnswerResponse(
    val nextQuestion: String,
    val score: Int,
)
