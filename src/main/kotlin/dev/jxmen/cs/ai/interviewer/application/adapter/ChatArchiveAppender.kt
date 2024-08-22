package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatArchiveCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatArchiveContentCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatContent
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.JpaChatArchiveContent
import org.springframework.stereotype.Component

@Component
class ChatArchiveAppender(
    private val chatArchiveCommandRepository: ChatArchiveCommandRepository,
    private val chatArchiveContentCommandRepository: ChatArchiveContentCommandRepository,
) {
    fun addArchive(
        subject: Subject,
        member: Member,
    ): JpaChatArchive {
        val chatArchive = JpaChatArchive(subject, member)
        val saved = chatArchiveCommandRepository.save(chatArchive)

        return saved
    }

    fun addContents(
        archive: JpaChatArchive,
        contents: List<ChatContent>,
    ) {
        // NOTE: saveAll과 내부적인 동작은 같다.
        contents.forEach { content ->
            val jpaChatArchiveContent = JpaChatArchiveContent(archive, content)
            chatArchiveContentCommandRepository.save(jpaChatArchiveContent)
        }
    }
}
