package dev.jxmen.cs.ai.interviewer.infrastructure

/**
 * https://docs.anthropic.com/en/docs/about-claude/models
 */
enum class ClaudeModel(
    private val s: String,
    private val description: String? = null,
) {
    // v3.5 models
    SONNET_3_5("claude-3-5-sonnet-20240620", "Sonnet 개선된 모델"),

    // v3 models
    OPUS_3("claude-3-opus-20240229", "응답속도는 가장 느리지만 정교한 모델"),
    SONNET_3("claude-3-sonnet-20240229", "응답속도/정확도 모두 중간 정도인 모델"),
    // NOTE: haiku3는 응답속도는 빠르나, 정확도가 낮아 혼자서 답변을 하는 경우도 발생하여 사용하지 않을 예정
    ;

    override fun toString(): String = s
}
