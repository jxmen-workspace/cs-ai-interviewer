import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Development 테스트로 변경하여 CI 환경에서는 실행되지 않도록 변경
class DateTest :
    StringSpec({
        "LocalDateTime to String Test" {
            val now = LocalDateTime.of(2024, 8, 15, 21, 0, 0)

            now.format(DateTimeFormatter.ISO_DATE_TIME) shouldBe "2024-08-15T21:00:00"
        }
    })
