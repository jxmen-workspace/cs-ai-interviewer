package dev.jxmen.cs.ai.interviewer.persistence.entity.chat

import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember

data class JpaChats(
    val jpaChats: List<JpaChat>,
) {
    fun validateHasAnswer() {
        require(hasAnswer()) { NoAnswerException() }
    }

    fun validateMatchMember(jpaMember: JpaMember) {
        require(jpaChats.all { it.jpaMember.equalsId(jpaMember) }) { "멤버가 일치하지 않습니다." }
    }

    fun validateNotUseAllAnswers() {
        require(!useAllAnswers()) { throw AllAnswersUsedException() }
    }

    /**
     * 답변이 최대값을 넘었다면 true, 아니면 false
     */
    private fun useAllAnswers(): Boolean = jpaChats.count { it.isAnswer() } >= JpaChat.MAX_ANSWER_COUNT

    private fun hasAnswer(): Boolean = jpaChats.any { it.isAnswer() }
}
