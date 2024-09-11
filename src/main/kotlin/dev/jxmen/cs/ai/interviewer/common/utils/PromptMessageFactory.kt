package dev.jxmen.cs.ai.interviewer.common.utils

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Component

@Component
class PromptMessageFactory {
    companion object {
        /**
         * AI에게 면졉관 역할을 부여하는 메시지
         *
         * @see PromptMessageParser 점수 파싱 담당
         */
        val grantInterviewerRoleMessage =
            """
                당신은 이제부터 Computer Science에 대해 질문하는 면접관이다. 
                당신이 안내한 질문에 대해 내가 답변을 하면, 그에 대한 점수를 그동안 해왔던 답변도 포함하여 10점 단위로 평가 및 이유와 공부할 수 있는 키워드, 꼬리 질문을 제시해주길 바란다.
                
                점수에 대한 기준은 다음과 같다.
                (0: 기초 부족 혹은 적절한 대답이 아님, 10~30: 기초 수준만 아는 상태, 40~60: 어느 정도 알고 있음, 70~90: 어느 정도 깊게 알고 있음, 100: 매우 깊게 알고 있음)
                
                답변 형식은 다음과 같다. (매우 중요: 내가 어떠한 답변을 해도 답변 내용에 따라 아래의 형식을 줄바꿈하여 맞추고, 면접관은 당신은 직접적인 정답을 제공해서는 안된다.)
                
                답변에 대한 점수: nn점
                이유: ~~에 대한 설명은 부족합니다 / ~~에 대한 설명은 틀렸습니다 / ~~에 대해 더 깊게 공부하세요.
                공부할 수 있는 키워드: ~~ / ~~ / ~~
                꼬리 질문: ~~에 대해 더 깊게 설명해보세요.
            """.trimMargin().trim()
    }

    fun create(
        answer: String,
        chats: Chats,
        subject: Subject,
    ): List<Message> {
        val initialMessages = createInitialMessages(subject.question)
        val userAnswerMessage = UserMessage(answer) // 멤버가 제공한 답변

        return when (chats.isEmpty()) {
            true -> initialMessages + userAnswerMessage
            false -> initialMessages + createMessagesFromBeforeChats(chats) + userAnswerMessage
        }
    }

    /**
     * AI가 답변 및 질문하는 내용 반환
     */
    fun getAiAnswerContentFromQuestion(question: String) =
        """
        네, 알겠습니다. 제공해주신 형식에 맞추어 답변하는 면접관 역할을 수행하고, 질문에 대한 답은 제공하지 않겠습니다. 
        제가 면접관으로 질문드릴 내용은 다음과 같습니다.
        
        질문: $question
        """.trimIndent().trim()

    private fun createInitialMessages(firstQuestion: String): List<Message> =
        listOf(
            UserMessage(grantInterviewerRoleMessage), // 면접관 역할 부여
            AssistantMessage(getAiAnswerContentFromQuestion(firstQuestion)), // 면접관 질문
        )

    private fun createMessagesFromBeforeChats(chats: Chats): List<Message> {
        require(!chats.isEmpty()) { "이전 채팅 내역이 존재해야 합니다." }
        require(chats.chats[0].isQuestion()) { "첫번째 채팅 내역은 질문이여야 합니다." }

        return chats.chats.drop(1).map {
            when (it.type) {
                ChatType.QUESTION -> AssistantMessage(it.message)
                ChatType.ANSWER -> UserMessage(it.message)
            }
        }
    }
}
