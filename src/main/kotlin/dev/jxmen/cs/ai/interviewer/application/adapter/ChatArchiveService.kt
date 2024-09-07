package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.ChatArchiveUseCase
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatArchiveAppender
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatRemover
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChats
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatArchiveService(
    // NOTE: 인터페이스로 만들면 너무 이른 추상화라 판단되어 구현체 클래스를 직업 의존하도록 하였다.
    private val chatRemover: ChatRemover,
    private val chatArchiveAppender: ChatArchiveAppender,
) : ChatArchiveUseCase {
    /**
     * 채팅을 아카이브하고 아카이브 ID를 반환한다.
     */
    @Transactional
    override fun archive(
        jpaChats: List<JpaChat>,
        jpaMember: JpaMember,
        jpaSubject: JpaSubject,
    ): Long {
        // validate chats and member
        val jpaChatsWrapper = JpaChats(jpaChats)
        jpaChatsWrapper.validateHasAnswer()
        jpaChatsWrapper.validateMatchMember(jpaMember)

        // remove all chats
        chatRemover.removeAll(jpaChats)

        // add archive and contents
        val archive = chatArchiveAppender.addArchive(jpaSubject, jpaMember)
        chatArchiveAppender.addContents(archive, jpaChats.map { it.content })

        return archive.id
    }
}
