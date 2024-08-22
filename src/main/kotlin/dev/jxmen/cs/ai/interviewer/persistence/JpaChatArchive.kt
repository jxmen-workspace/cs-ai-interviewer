package dev.jxmen.cs.ai.interviewer.persistence

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * 멤버는 여러개의 채팅 아카이브를 가진다. (1:N)
 * 채팅 아카이브는 하나의 주제에 속한다. (N:1)
 */
@Entity
@Suppress("ktlint:standard:no-blank-line-in-list")
@Table(
    name = "chat_archive",
    indexes = [
        Index(
            name = "chat_archive_member_id_subject_id_idx",
            columnList = "member_id, subject_id",
        ),
    ],
)
class JpaChatArchive(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,
) : BaseEntity() {
    constructor(id: Long, subject: Subject, member: Member) : this(subject, member) {
        this.id = id
    }
}
