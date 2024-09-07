package dev.jxmen.cs.ai.interviewer.persistence.adapter

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchiveContent
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatContent
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveContentCommandRepository
import org.springframework.stereotype.Component

@Component
class ChatArchiveAppender(
    private val chatArchiveCommandRepository: ChatArchiveCommandRepository,
    private val chatArchiveContentCommandRepository: ChatArchiveContentCommandRepository,
) {
    fun addArchive(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): JpaChatArchive {
        val chatArchive = JpaChatArchive(jpaSubject, jpaMember)
        val saved = chatArchiveCommandRepository.save(chatArchive)

        return saved
    }

    fun addContents(
        archive: JpaChatArchive,
        contents: List<JpaChatContent>,
    ) {
        // NOTE: saveAll과 내부적인 동작은 같다.
        contents.forEach { content ->
            val jpaChatArchiveContent = JpaChatArchiveContent(archive, content)
            chatArchiveContentCommandRepository.save(jpaChatArchiveContent)
        }
    }
}
