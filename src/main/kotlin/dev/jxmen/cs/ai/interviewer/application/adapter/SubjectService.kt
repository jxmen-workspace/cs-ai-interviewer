package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2
import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val aiApiClient: AIApiClient,
    private val chatUseCase: ChatUseCase,
) : SubjectUseCase {
    override fun answerV2(command: CreateSubjectAnswerCommandV2): SubjectAnswerResponse {
        // 1. 답변을 모두 사용하지 않았는지 확인
        val chats = Chats(command.chats)
        require(!chats.useAllAnswers()) { throw AllAnswersUsedException() }

        // 2. API 호출해서 다음 질문과 점수 받아오기
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, command.chats)

        // 3. 기존 답변과 다음 질문 저장
        chatUseCase.add(
            subject = command.subject,
            member = command.member,
            answer = command.answer,
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )

        return SubjectAnswerResponse(
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )
    }
}
