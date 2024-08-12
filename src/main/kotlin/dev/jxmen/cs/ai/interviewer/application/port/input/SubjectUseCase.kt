package dev.jxmen.cs.ai.interviewer.application.port.input

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2

interface SubjectUseCase {
    fun answerV2(command: CreateSubjectAnswerCommandV2): SubjectAnswerResponse
}
