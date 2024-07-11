package dev.jxmen.cs.ai.interviewer.application.port.`in`

import dev.jxmen.cs.ai.interviewer.application.port.`in`.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.response.SubjectAnswerResponse

interface SubjectUseCase {
    fun answer(command: CreateSubjectAnswerCommand): SubjectAnswerResponse
}
