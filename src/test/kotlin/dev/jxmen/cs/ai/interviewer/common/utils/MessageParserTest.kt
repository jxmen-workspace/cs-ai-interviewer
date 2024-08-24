package dev.jxmen.cs.ai.interviewer.common.utils

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

class MessageParserTest :
    DescribeSpec({
        describe("MessageParser") {
            context("parseScore") {
                it("점수가 포함된 문자열을 처리할 때 올바른 점수를 반환해야 합니다") {
                    val messageParser = MessageParser()

                    val testCases =
                        table(
                            headers("input", "expectedScore"),
                            row("답변에 대한 점수: 0점", 0),
                            row("답변에 대한 점수: 10점", 10),
                            row("답변에 대한 점수: 40점", 40),
                            row("답변에 대한 점수: 85점", 85),
                            row("답변에 대한 점수: 100점", 100),
                        )

                    forAll(testCases) { input, expectedScore ->
                        val score = messageParser.parseScore(input)
                        score shouldBe expectedScore
                    }
                }

                it("점수가 포함되지 은 문자열을 처리할 때 0을 반환해야 합니다") {
                    val messageParser = MessageParser()

                    val testCases =
                        table(
                            headers("input", "expectedScore"),
                            row("답변에 대한 점수: 없음", 0),
                            row("답변에 대한 점수: 0", 0),
                            row("답변에 대한 점수: 점", 0),
                            row("답변에 대한 점수: 100", 0),
                            row("답변에 대한 점수: 90", 0),
                            row("점수가 없습니다", 0),
                        )

                    forAll(testCases) { input, expectedScore ->
                        val score = messageParser.parseScore(input)
                        score shouldBe expectedScore
                    }
                }
            }
        }
    })
