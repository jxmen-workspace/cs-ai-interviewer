package dev.jxmen.cs.ai.interviewer.domain.subject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SubjectCategoryTest :
    DescribeSpec({
        describe("valueOf") {
            context("만약 문자열이 잘못된 enum 값이라면") {
                it("IllegalArgumentException을 던진다") {
                    shouldThrow<IllegalArgumentException> {
                        SubjectCategory.valueOf("DSA1")
                    }
                }
            }
            context("만약 문자열이 올바른 enum 값이라면") {
                it("해당 enum 값을 반환해야 한다") {
                    SubjectCategory.valueOf("DSA") shouldBe SubjectCategory.DSA
                    SubjectCategory.valueOf("NETWORK") shouldBe SubjectCategory.NETWORK
                    SubjectCategory.valueOf("DATABASE") shouldBe SubjectCategory.DATABASE
                    SubjectCategory.valueOf("OS") shouldBe SubjectCategory.OS
                }
            }
        }
    })
