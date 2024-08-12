package dev.jxmen.cs.ai.interviewer.domain.chat.exceptions

class AllAnswersUsedException(
    s: String = "모든 답변을 사용했습니다.",
) : RuntimeException(s)
