package dev.jxmen.cs.ai.interviewer.domain.subject

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SubjectCategoryTest :
    DescribeSpec({
        describe("fromString") {
            context("만약 문자열이 잘못된 enum 값이라면") {
                it("IllegalArgumentException을 던져야 한다") {
                    runCatching { SubjectCategory.fromString("dsa1") }
                        .exceptionOrNull() shouldBe IllegalArgumentException("No such enum constant dsa1")
                }
            }
            context("만약 문자열이 올바른 enum 값이라면") {
                it("해당 enum 값을 반환해야 한다") {
                    SubjectCategory.fromString("dsa") shouldBe SubjectCategory.DSA
                    SubjectCategory.fromString("network") shouldBe SubjectCategory.NETWORK
                    SubjectCategory.fromString("database") shouldBe SubjectCategory.DATABASE
                    SubjectCategory.fromString("os") shouldBe SubjectCategory.OS
                }
            }
        }
    })
