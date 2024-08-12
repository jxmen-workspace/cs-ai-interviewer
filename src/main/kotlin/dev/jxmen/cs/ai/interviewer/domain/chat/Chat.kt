package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
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

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("채팅 내용")
    val message: String,

    @Column(nullable = true)
    @Comment("점수")
    val score: Int? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = ChatTypeConverter::class)
    @Comment("채팅 유형")
    val chatType: ChatType, // TODO: type으로 이름 변경
) : BaseEntity() {
    constructor(subject: Subject, member: Member, message: String, chatType: ChatType, score: Int) : this(
        subject = subject,
        member = member,
        userSessionId = null,
        message = message,
        score = score,
        chatType = chatType,
    )

    companion object {
        const val MAX_ANSWER_COUNT = 10

        fun createQuestion(
            subject: Subject,
            member: Member,
            nextQuestion: String,
        ): Chat =
            Chat(
                subject = subject,
                member = member,
                message = nextQuestion,
                chatType = ChatType.QUESTION,
            )

        fun createAnswer(
            subject: Subject,
            member: Member,
        ) = Chat(
            subject = subject,
            member = member,
            message = " ",
            score = 0,
            chatType = ChatType.ANSWER,
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
                message = answer,
                score = score,
                chatType = ChatType.ANSWER,
            )
    }
}
