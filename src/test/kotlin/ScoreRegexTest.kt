import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// TODO: Development test로 변경
class ScoreRegexTest :
    StringSpec({

        "문자에 점수 정규식에 포함된다면 점수 추출" {
            val regex = "답변에 대한 점수: (\\d+)점".toRegex()
            val text =
                """
                답변에 대한 점수: 100점
                이유: 이유
                공부할 수 있는 키워드: 키워드
                꼬리 질문: 꼬리 질문
                """.trimIndent()

            val score = extractScore(regex, text)

            score shouldBe 100
        }

        "문자가 일치하지 않다면 0점" {
            val regex = "답변에 대한 점수: (\\d+)점".toRegex()
            val text =
                // NOTE: "점"이 빠져있음
                """
                답변에 대한 점수: 100
                이유: 이유
                공부할 수 있는 키워드: 키워드
                꼬리 질문: 꼬리 질문
                """.trimIndent()

            val score = extractScore(regex, text)

            score shouldBe 0
        }
    })

private fun extractScore(
    regex: Regex,
    text: String,
): Int {
    val score =
        regex
            .find(text)
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: 0
    return score
}
