package dev.jxmen.cs.ai.interviewer.domain.subject.service.adapter

import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatQuery
import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.service.port.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.domain.subject.service.port.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.external.port.AIApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val aiApiClient: AIApiClient,
    private val chatQuery: ChatQuery,
    private val chatUseCase: ChatUseCase,
) : SubjectUseCase {
    companion object {
        val logger = LoggerFactory.getLogger(SubjectService::class.java)
    }

    override fun answer(command: CreateSubjectAnswerCommand): SubjectAnswerResponse {
        // 1. subjectId와 userSessionId에 대한 채팅 내역 조회
        val chats = chatQuery.findBySubjectAndUserSessionId(command.subject, command.userSessionId)

        // 2. API 호출해서 다음 질문과 점수 받아오기
        logger.info("requesting answer to apiClient - sessionId: ${command.userSessionId}")
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, chats)
        logger.info(
            "api request success. userSessionId: ${command.userSessionId}, nextQuestion: ${apiResponse.nextQuestion}, score: ${apiResponse.score}",
        )

        // 3. 기존 답변과 다음 질문 저장
        chatUseCase.add(
            subject = command.subject,
            userSessionId = command.userSessionId,
            answer = command.answer,
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
            chats = chats,
        )

        return SubjectAnswerResponse(
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )
    }
}
