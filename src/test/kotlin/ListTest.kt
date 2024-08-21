import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// TODO: Development test로 변경
class ListTest :
    StringSpec({

        "drop 1 from list test" {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.drop(1)
            result shouldBe listOf(2, 3, 4, 5)
        }
    })
