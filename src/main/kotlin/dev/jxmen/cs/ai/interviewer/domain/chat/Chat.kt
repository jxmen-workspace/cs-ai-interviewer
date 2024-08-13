package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Suppress("ktlint:standard:no-blank-line-in-list")
@Entity
@Table(indexes = [Index(name = "idx_subject_userSessionId", columnList = "subject_id,userSessionId")])
class Chat(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @Comment("주제")
    val subject: Subject,

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // NOTE: sessionId 컬럼 제거 시 nullable 제거
    @JoinColumn(name = "member_id")
    @Comment("멤버 아이디")
    val member: Member? = null,

    @Column(nullable = true)
    @Comment("유저 세션 아이디")
    val userSessionId: String? = null, // NOTE: 유저 도메인이 추가되면 memberId로 변경 예정

    @get:Embedded
    val content: ChatContent,

) : BaseEntity() {
    fun isAnswer(): Boolean = content.isAnswer()

    constructor(subject: Subject, member: Member, message: String, chatType: ChatType, score: Int) : this(
        subject = subject,
        member = member,
        userSessionId = null,
        content =
            ChatContent(
                message = message,
                chatType = chatType,
                score = score,
            ),
    )

    companion object {
        const val MAX_ANSWER_SCORE = 100
        const val MAX_ANSWER_COUNT = 10

        fun createQuestion(
            subject: Subject,
            member: Member,
            nextQuestion: String,
        ): Chat =
            Chat(
                subject = subject,
                member = member,
                content = ChatContent.createQuestion(nextQuestion),
            )

        fun createAnswer(
            subject: Subject,
            member: Member,
        ) = Chat(
            subject = subject,
            member = member,
            content = ChatContent.createEmptyAnswer(),
        )

        fun createAnswer(
            subject: Subject,
            member: Member,
            answer: String,
            score: Int,
        ): Chat =
            Chat(
                subject = subject,
                member = member,
                content = ChatContent.createAnswer(answer, score),
            )
    }
}
