package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2
import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val chatQuery: ChatQuery,
    private val aiApiClient: AIApiClient,
    private val chatUseCase: ChatUseCase,
) : SubjectUseCase {

    @Deprecated("answerV2로 대체될 예정")
    override fun answer(command: CreateSubjectAnswerCommand): SubjectAnswerResponse {
        // 1. subjectId와 userSessionId에 대한 채팅 내역 조회
        val chats = chatQuery.findBySubjectAndUserSessionId(command.subject, command.userSessionId)

        // 2. API 호출해서 다음 질문과 점수 받아오기
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, chats)

        // 3. 기존 답변과 다음 질문 저장
        chatUseCase.add(
            subject = command.subject,
            userSessionId = command.userSessionId,
            answer = command.answer,
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )

        return SubjectAnswerResponse(
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )
    }

    override fun answerV2(command: CreateSubjectAnswerCommandV2): SubjectAnswerResponse {
        // 1. subjectId와 memberId에 대한 채팅 내역 조회
        val chats = chatQuery.findBySubjectAndMember(command.subject, command.member)

        // 2. API 호출해서 다음 질문과 점수 받아오기
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, chats)

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
