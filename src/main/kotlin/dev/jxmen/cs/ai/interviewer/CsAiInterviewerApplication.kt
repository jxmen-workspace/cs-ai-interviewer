package dev.jxmen.cs.ai.interviewer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
open class CsAiInterviewerApplication

fun main(args: Array<String>) {
    runApplication<CsAiInterviewerApplication>(*args)
}

@RestController
class IndexController {
    @GetMapping("/")
    fun hello(): String {
        return "Hello, World!"
    }
}
