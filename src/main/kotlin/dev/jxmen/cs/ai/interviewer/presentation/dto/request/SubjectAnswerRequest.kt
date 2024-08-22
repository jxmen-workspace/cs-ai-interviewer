package dev.jxmen.cs.ai.interviewer.presentation.dto.request

import jakarta.validation.constraints.NotBlank

data class SubjectAnswerRequest(
    @field:NotBlank(message = "답변은 공백일 수 없습니다.")
    val answer: String,
)
