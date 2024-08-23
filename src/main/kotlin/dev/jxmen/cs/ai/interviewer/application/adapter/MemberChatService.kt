package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberChatService(
    // NOTE: 인터페이스로 만들면 너무 이른 추상화라 판단되어 구현체 클래스를 직업 의존하도록 하였다.
    private val chatRemover: ChatRemover,
    private val chatArchiveAppender: ChatArchiveAppender,
) : MemberChatUseCase {
    /**
     * 채팅을 아카이브하고 아카이브 ID를 반환한다.
     */
    @Transactional
    override fun archive(
        chats: List<Chat>,
        member: Member,
        subject: Subject,
    ): Long {
        // validate chats and member
        val chatsWrapper = Chats(chats)
        chatsWrapper.validateHasAnswer()
        chatsWrapper.validateMatchMember(member)

        // remove all chats
        chatRemover.removeAll(chats)

        // add archive and contents
        val archive = chatArchiveAppender.addArchive(subject, member)
        chatArchiveAppender.addContents(archive, chats.map { it.content })

        return archive.id
    }
}
