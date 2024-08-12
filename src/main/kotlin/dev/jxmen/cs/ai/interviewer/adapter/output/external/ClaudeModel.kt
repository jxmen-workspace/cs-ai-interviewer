package dev.jxmen.cs.ai.interviewer.adapter.output.external

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
    HAIKU_3("claude-3-haiku-20240307", "응답속도는 가장 빠르지만 정확도는 낮은 모델"),
    ;

    override fun toString(): String = s
}
