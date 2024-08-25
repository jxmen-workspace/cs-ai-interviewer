package dev.jxmen.cs.ai.interviewer.persistence.entity.chat

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatContent
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "chat_archive_content",
    indexes = [
        Index(
            name = "chat_archive_content_chat_archive_id_idx",
            columnList = "chat_archive_id",
        ),
    ],
)
@Suppress("ktlint:standard:no-blank-line-in-list")
class JpaChatArchiveContent(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_archive_id")
    @Comment("채팅 아카이브 아이디")
    val archive: JpaChatArchive,

    @Embedded
    val content: ChatContent,
) : BaseEntity()
