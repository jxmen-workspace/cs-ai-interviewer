package dev.jxmen.cs.ai.interviewer.domain.subject.service.port

import dev.jxmen.cs.ai.interviewer.domain.subject.dto.response.SubjectAnswerResponse

interface SubjectUseCase {
    fun answer(command: CreateSubjectAnswerCommand): SubjectAnswerResponse
}
