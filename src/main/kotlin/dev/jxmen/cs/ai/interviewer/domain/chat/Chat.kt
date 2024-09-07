package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import java.time.LocalDateTime

class Chat(
    val id: Long,
    val subject: Subject,
    val member: Member,
    val message: String,
    val type: ChatType,
    val score: Int?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    fun isAnswer(): Boolean = type == ChatType.ANSWER

    fun isQuestion(): Boolean = type == ChatType.QUESTION

    companion object {
        const val MAX_ANSWER_SCORE = 100
        const val MAX_ANSWER_COUNT = 10
    }
}
