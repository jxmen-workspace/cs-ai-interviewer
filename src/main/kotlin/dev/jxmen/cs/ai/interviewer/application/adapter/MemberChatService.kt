package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberChatService(
    // NOTE: 채팅 제거, 채팅 아카아브 생성을 port로 만들면 너무 오버 엔지니어링이라 판단되어 구현체 클래스를 직업 의존하도록 하였다.
    private val aiApiClient: AIApiClient,
    private val chatAppender: ChatAppender,
    private val chatRemover: ChatRemover,
    private val chatArchiveAppender: ChatArchiveAppender,
) : MemberChatUseCase {
    @Deprecated("V3로 대체 예정")
    override fun answerV2(command: CreateSubjectAnswerCommand): SubjectAnswerResponse? {
        // 1. 답변을 모두 사용하지 않았는지 확인
        val chatsWrapper = Chats(command.chats)
        require(!chatsWrapper.useAllAnswers()) { throw AllAnswersUsedException() }

        // 2. API 호출해서 다음 질문과 점수 받아오기
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, command.chats)

        // 3. 기존 답변과 다음 질문 저장
        chatAppender.addAnswerAndNextQuestionV2(
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

    override fun answerV3(command: CreateSubjectAnswerCommand): SubjectAnswerResponse? {
        // 1. 답변을 모두 사용하지 않았는지 확인
        val chatsWrapper = Chats(command.chats)
        require(!chatsWrapper.useAllAnswers()) { throw AllAnswersUsedException() }

        // 2. API 호출해서 다음 질문과 점수 받아오기
        val apiResponse = aiApiClient.requestAnswer(command.subject, command.answer, command.chats)

        // 3. 기존 답변과 다음 질문 저장
        chatAppender.addAnswerAndNextQuestionV3(
            subject = command.subject,
            member = command.member,
            answer = command.answer,
            chats = command.chats,
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )

        return SubjectAnswerResponse(
            nextQuestion = apiResponse.nextQuestion,
            score = apiResponse.score,
        )
    }

    @Transactional
    override fun archive(
        chats: List<Chat>,
        member: Member,
        subject: Subject,
    ): Long {
        // validate chats and member
        val chatsWrapper = Chats(chats)
        require(chatsWrapper.hasAnswer()) { throw NoAnswerException() }

        // remove all chats
        chatRemover.removeAll(chats)

        // add archive and contents
        val archive = chatArchiveAppender.addArchive(subject, member)
        chatArchiveAppender.addContents(archive, chats.map { it.content })

        return archive.id
    }
}
