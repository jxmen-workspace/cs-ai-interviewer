package dev.jxmen.cs.ai.interviewer.persistence.entity.chat

import dev.jxmen.cs.ai.interviewer.persistence.entity.BaseEntity
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Suppress("ktlint:standard:no-blank-line-in-list")
@Entity
@Table(
    indexes = [
        Index(name = "idx_chat_subject_id_member_id", columnList = "subject_id,member_id"),
    ],
)
class JpaChat(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @Comment("주제")
    val jpaSubject: JpaSubject,

    @ManyToOne(fetch = FetchType.LAZY) // NOTE: sessionId 컬럼 제거 시 nullable 제거
    @JoinColumn(name = "member_id")
    @Comment("멤버 아이디")
    val jpaMember: JpaMember,

    @get:Embedded
    val content: JpaChatContent,

) : BaseEntity() {
    fun isAnswer(): Boolean = content.isAnswer()

    fun isQuestion(): Boolean = content.isQuestion()

    companion object {
        const val MAX_ANSWER_SCORE = 100
        const val MAX_ANSWER_COUNT = 10

        fun createQuestion(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
            message: String,
        ): JpaChat {
            val content = JpaChatContent.createQuestion(message)
            val jpaChat = JpaChat(jpaSubject = jpaSubject, jpaMember = jpaMember, content = content)

            return jpaChat
        }

        fun createAnswer(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
            answer: String,
            score: Int,
            createdAt: LocalDateTime? = null,
        ): JpaChat {
            val content = JpaChatContent.createAnswer(answer, score)
            val jpaChat = JpaChat(jpaSubject = jpaSubject, jpaMember = jpaMember, content = content)

            createdAt?.let { jpaChat.createdAt = it }
            return jpaChat
        }

        fun createFirstQuestion(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
        ): JpaChat {
            val content = JpaChatContent.createQuestion(jpaSubject.question)
            val jpaChat = JpaChat(jpaSubject = jpaSubject, jpaMember = jpaMember, content = content)

            return jpaChat
        }
    }
}
