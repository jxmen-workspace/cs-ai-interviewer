package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand

interface SubjectUseCase {
    fun answer(command: CreateSubjectAnswerCommand): SubjectAnswerResponse
}
