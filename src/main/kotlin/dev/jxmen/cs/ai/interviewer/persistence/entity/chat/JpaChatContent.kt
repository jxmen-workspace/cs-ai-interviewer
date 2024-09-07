package dev.jxmen.cs.ai.interviewer.persistence.entity.chat

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Lob
import org.hibernate.annotations.Comment

@Embeddable
@Suppress("ktlint:standard:no-blank-line-in-list")
data class JpaChatContent(
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("채팅 내용")
    var message: String,

    @Column(nullable = true)
    @Comment("점수")
    var score: Int? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = JpaChatTypeConverter::class)
    @Comment("채팅 유형")
    var chatType: ChatType, // TODO: type으로 이름 변경
) {
    fun isAnswer(): Boolean = chatType == ChatType.ANSWER

    fun isQuestion(): Boolean = chatType == ChatType.QUESTION

    companion object {
        fun createQuestion(nextQuestion: String): JpaChatContent =
            JpaChatContent(
                message = nextQuestion,
                chatType = ChatType.QUESTION,
                score = null,
            )

        fun createAnswer(
            answer: String,
            score: Int? = 0,
        ): JpaChatContent {
            require(answer.isNotBlank()) { "답변이 비어있습니다." }
            require(score in 0..JpaChat.MAX_ANSWER_SCORE) { "점수는 0~${JpaChat.MAX_ANSWER_SCORE} 사이여야 합니다." }
            return JpaChatContent(
                message = answer,
                chatType = ChatType.ANSWER,
                score = score,
            )
        }
    }
}
