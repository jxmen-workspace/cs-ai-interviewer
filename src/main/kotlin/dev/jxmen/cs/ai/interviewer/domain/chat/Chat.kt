package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Suppress("ktlint:standard:no-blank-line-in-list")
@Entity
@Table(indexes = [Index(name = "idx_subject_userSessionId", columnList = "subject_id,userSessionId")])
class Chat(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @Comment("주제")
    val subject: Subject,

    @Column(nullable = false)
    @Comment("유저 세션 아이디")
    val userSessionId: String, // NOTE: 유저 도메인이 추가되면 userId로 변경 예정

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
    val chatType: ChatType,
) : BaseEntity()
