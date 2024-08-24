import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// TODO: Development test로 변경
class ListTest :
    StringSpec({

        "drop 1 from list test" {
            val list = listOf(2, 3, 4, 5, 6)
            val result = list.drop(1)
            result shouldBe listOf(3, 4, 5, 6)
        }

        "만약 빈 리스트에서 drop을 시도하면 emptyList를 반환한다" {
            val list = emptyList<Int>()
            val result = list.drop(1)

            result shouldBe emptyList()
        }

        "기존 리스트에서 빈 리스트를 합치면 기존 리스트를 그대로 반환한다" {
            val list = listOf(1, 2, 3, 4, 5)
            val emptyList = emptyList<Int>()

            list + emptyList shouldBe listOf(1, 2, 3, 4, 5)
            list + emptyList + listOf(999) shouldBe listOf(1, 2, 3, 4, 5, 999)
        }
    })
