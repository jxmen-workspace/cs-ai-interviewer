package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException

data class Chats(
    val chats: List<Chat> = emptyList(),
) {
    fun validateNotUseAllAnswers() {
        require(!useAllAnswers()) { throw AllAnswersUsedException() }
    }

    fun isEmpty(): Boolean = chats.isEmpty()

    /**
     * 답변이 최대값을 넘었다면 true, 아니면 false
     */
    private fun useAllAnswers(): Boolean = chats.count { it.isAnswer() } >= Chat.MAX_ANSWER_COUNT
}
