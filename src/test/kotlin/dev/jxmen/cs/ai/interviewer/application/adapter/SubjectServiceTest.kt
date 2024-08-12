package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2
import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.application.port.output.dto.AiApiAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SubjectServiceTest :
    StringSpec({

        val aiApiClient: AIApiClient = mockk()
        val chatUseCase: ChatUseCase = mockk()
        val subjectService = SubjectService(aiApiClient, chatUseCase)

        val subject =
            Subject(
                title = "subject",
                question = "question",
                category = SubjectCategory.OS,
            )
        val member = Member.createGoogleMember("jxmen", "me@jxmen.dev")

        "should throw AllAnswersUsedException when all answers are used" {
            val command =
                CreateSubjectAnswerCommandV2(
                    subject = subject,
                    member = member,
                    answer = "answer",
                    chats = List(Chat.MAX_ANSWER_COUNT) { Chat.createAnswer(subject, member) },
                )

            shouldThrow<AllAnswersUsedException> {
                subjectService.answerV2(command)
            }
        }

        "should call API and save response when answers are not all used" {
            val command =
                CreateSubjectAnswerCommandV2(
                    subject = subject,
                    member = member,
                    answer = "answer",
                    chats = List(Chat.MAX_ANSWER_COUNT - 1) { Chat.createAnswer(subject, member) },
                )
            val apiResponse = AiApiAnswerResponse("nextQuestion", 50)
            every { aiApiClient.requestAnswer(any(), any(), any()) } returns apiResponse
            every { chatUseCase.add(any(), any(), any(), any(), any()) } returns Unit

            subjectService.answerV2(command)

            verify { aiApiClient.requestAnswer(command.subject, command.answer, command.chats) }
            verify { chatUseCase.add(command.subject, command.member, command.answer, apiResponse.nextQuestion, apiResponse.score) }
        }
    })
