package dev.jxmen.cs.ai.interviewer.infra.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class ExecutionTimeLoggingAspect {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Pointcut(
        "within(dev.jxmen.cs.ai.interviewer.domain..*.service.adapter..*) || within(dev.jxmen.cs.ai.interviewer.external.adapter..*)",
    )
    fun adapterMethods() {
        // Pointcut - do nothing here
    }

    @Around("adapterMethods()")
    fun logExecutionTime(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        try {
            return proceedingJoinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()

            val simpleClassName =
                proceedingJoinPoint.signature.declaringTypeName
                    .split(".")
                    .last() // 맨 마지막 클래스명만 사용
            val methodName = proceedingJoinPoint.signature.name

            logger.info("Execution time of $simpleClassName.$methodName: ${endTime - startTime} ms")
        }
    }
}
