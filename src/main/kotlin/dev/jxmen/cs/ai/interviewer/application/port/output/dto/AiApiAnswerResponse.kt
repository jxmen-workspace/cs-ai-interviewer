package dev.jxmen.cs.ai.interviewer.application.port.output.dto

data class AiApiAnswerResponse(
    val nextQuestion: String,
    val score: Int,
)
