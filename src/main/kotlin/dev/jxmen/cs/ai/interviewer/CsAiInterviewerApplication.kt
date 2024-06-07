package dev.jxmen.cs.ai.interviewer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
class CsAiInterviewerApplication

fun main(args: Array<String>) {
    runApplication<CsAiInterviewerApplication>(*args)
}

@Controller
class IndexController {
    @GetMapping("/")
    fun index(): String {
        return "index"
    }
}
