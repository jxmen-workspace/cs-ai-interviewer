package dev.jxmen.cs.ai.interviewer.domain.chat

class Chats(
    private val chats: List<Chat>,
) {
    /**
     * 답변이 최대값을 넘었다면 true, 아니면 false
     */
    fun useAllAnswers(): Boolean = chats.count { it.chatType == ChatType.ANSWER } >= Chat.MAX_ANSWER_COUNT
}
