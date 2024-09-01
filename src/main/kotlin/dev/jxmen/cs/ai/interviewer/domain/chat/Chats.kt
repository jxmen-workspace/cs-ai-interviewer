package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.domain.member.Member

data class Chats(
    val chats: List<Chat>,
) {
    fun validateHasAnswer() {
        require(hasAnswer()) { NoAnswerException() }
    }

    fun validateMatchMember(member: Member) {
        require(chats.all { it.member.equalsId(member) }) { "멤버가 일치하지 않습니다." }
    }

    fun validateNotUseAllAnswers() {
        require(!useAllAnswers()) { throw AllAnswersUsedException() }
    }

    /**
     * 답변이 최대값을 넘었다면 true, 아니면 false
     */
    private fun useAllAnswers(): Boolean = chats.count { it.isAnswer() } >= Chat.MAX_ANSWER_COUNT

    private fun hasAnswer(): Boolean = chats.any { it.isAnswer() }
}
