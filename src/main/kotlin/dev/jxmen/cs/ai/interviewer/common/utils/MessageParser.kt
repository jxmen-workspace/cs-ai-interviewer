package dev.jxmen.cs.ai.interviewer.common.utils

import org.springframework.stereotype.Component

@Component
class MessageParser {
    companion object {
        /**
         * @see PromptMessageFactory 프롬프트 메시지 생성 담당
         */
        private val SCORE_REGEX = "답변에 대한 점수: (\\d+)점".toRegex()
    }

    fun parseScore(sb: StringBuilder): Int =
        SCORE_REGEX
            .find(sb.toString())
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0
}
