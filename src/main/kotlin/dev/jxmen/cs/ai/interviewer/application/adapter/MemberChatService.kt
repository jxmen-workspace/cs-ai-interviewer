package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.application.port.input.MemberChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberChatService(
    // NOTE: 채팅 제거, 채팅 아카아브 생성을 port로 만들면 너무 오버 엔지니어링이라 판단되어 구현체 클래스를 직업 의존하도록 하였다.
    private val chatRemover: ChatRemover,
    private val chatArchiveAdder: ChatArchiveAdder,
) : MemberChatUseCase {
    override fun archive(
        chats: List<Chat>,
        member: Member,
        subject: Subject,
    ): Long {
        // validate chats and member
        val chatsWrapper = Chats(chats)
        require(chatsWrapper.hasAnswer()) { throw NoAnswerException() }

        // remove all chats
        chatRemover.removeAll(chats)

        // add archive and contents
        val archive = chatArchiveAdder.addArchive(subject, member)
        chatArchiveAdder.addContents(archive, chats.map { it.content })

        return archive.id
    }
}
