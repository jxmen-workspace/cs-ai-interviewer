import groovy.lang.Closure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("com.epages.restdocs-api-spec") version "0.17.1"

    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.0" // auto generate no args constructor
}

group = "dev.jxmen"
version = "1.3.3"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()

    // NOTE: Spring AI 추가를 위해 필요
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

object Versions {
    const val EPAGES = "0.17.1"
    const val MOCKK = "1.13.11"
    const val MOCKITO_KOTLIN = "5.4.0"
    const val KOTEST = "5.8.1"
    const val KOTLIN_JDSL = "3.5.1"
    const val FIXTURE_MONKEY_KOTLIN = "1.0.23"
    const val SPRING_AI_ANTHROPIC = "1.0.0-M1"
    const val JJWT = "0.12.6"
    const val KOTEST_SPRING = "1.3.0"
    const val MARIADB_JAVA_CLIENT = "3.1.0"
    const val NETTY_RESOLVER_DNS_NATIVE_MACOS = "4.1.72.Final"
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // spring ai
    implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter:${Versions.SPRING_AI_ANTHROPIC}")
    implementation("org.springframework.ai:spring-ai-anthropic:${Versions.SPRING_AI_ANTHROPIC}")

    // template engine
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // spring security, oauth2
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-client")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.h2database:h2")
    implementation("org.springframework.session:spring-session-jdbc") // redis로 변경은 추후 검토

    // kotlin jdsl dependencies
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:${Versions.KOTLIN_JDSL}") // JPQL을 만들어 주도록 도와주는 라이브러리
    implementation("com.linecorp.kotlin-jdsl:jpql-render:${Versions.KOTLIN_JDSL}") // DSL로 만든 쿼리를 String으로 변환해주는 라이브러리
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:${Versions.KOTLIN_JDSL}") // Spring Data JPA를 지원하는 Kotlin JDSL 라이브러리

    /**
     * for generating OpenAPI 3.0 spec
     *
     * https://mvnrepository.com/artifact/com.epages/restdocs-api-spec-mockmvc
     */
    implementation("com.epages:restdocs-api-spec:${Versions.EPAGES}")
    implementation("com.epages:restdocs-api-spec-mockmvc:${Versions.EPAGES}")
    implementation("com.epages:restdocs-api-spec-webtestclient:${Versions.EPAGES}")
    implementation("com.epages:restdocs-api-spec-openapi3-generator:${Versions.EPAGES}")
    implementation("org.springframework.restdocs:spring-restdocs-webtestclient")

    // kotest spring extension
    implementation("io.kotest.extensions:kotest-extensions-spring:${Versions.KOTEST_SPRING}")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${Versions.JJWT}") // 인터페이스
    implementation("io.jsonwebtoken:jjwt-gson:${Versions.JJWT}") // Gson을 사용하는 구현체

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // for test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("io.mockk:mockk:${Versions.MOCKK}")
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-assertions-core:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-framework-datatest:${Versions.KOTEST}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")

    // fixture-monkey
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:${Versions.FIXTURE_MONKEY_KOTLIN}")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // jjwt 실제 구현체
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.JJWT}") // 실제 구현체

    // mariadb client
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:${Versions.MARIADB_JAVA_CLIENT}") {
        exclude(group = "com.github.waffle", module = "waffle-jna")
    }

    // MacOS에서 Netty 사용 시 Apple Silicon 지원을 위해 추가
    if (isAppleSilicon()) {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:${Versions.NETTY_RESOLVER_DNS_NATIVE_MACOS}:osx-aarch_64")
    }
}

fun isAppleSilicon() = System.getProperty("os.name") == "Mac OS X" && System.getProperty("os.arch") == "aarch64"

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_21
    }
}

val copyOasToSwaggerTask = "copyOasToSwagger"

tasks.withType<Test> {
    useJUnitPlatform()

    finalizedBy(copyOasToSwaggerTask)
}

tasks.jar {
    enabled = false // plain jar 파일 생성 비활성화
}

tasks.bootJar {
    dependsOn("openapi3") // openapi3.yaml 파일이 먼저 생성되도록 설정

    from(layout.buildDirectory.file("api-spec/openapi3.yaml")) {
        into("BOOT-INF/classes/static/swagger-ui")
    }

    archiveFileName.set("app.jar")
}

val owner = "jxmen"

// 코틀린 람다를 Groovy Closure로 감싸는 함수
fun <T> kotlinLambdaToGroovyClosure(lambda: (T) -> Unit): Closure<T> =
    object : Closure<T>(owner) {
        fun doCall(it: T) {
            lambda(it)
        }
    }

openapi3 {
    setServers(
        listOf(
            kotlinLambdaToGroovyClosure { server -> server.url = "http://localhost:8080" },
            kotlinLambdaToGroovyClosure { server -> server.url = "https://cs-ai-api.jxmen.dev" },
        ),
    )

    format = "yaml"
}

tasks.register<Copy>(copyOasToSwaggerTask) {
    dependsOn("openapi3") // openapi3 Task가 먼저 실행되도록 설정

    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "openapi 파일 정적 경로에 복사"

    delete("src/main/resources/static/swagger-ui/openapi3.yaml") // 기존 OAS 파일 삭제
    from(layout.buildDirectory.file("api-spec/openapi3.yaml")) // 복제할 OAS 파일 지정
    into("src/main/resources/static/swagger-ui/.") // 타겟 디렉터리로 파일 복제
}

tasks.register<Copy>("writeVersion") {
    group = "custom"
    description = "버전 정보 기록"

    val versionFile = file("$projectDir/src/main/resources/version.properties")
    println("Writing version=${project.version} to version.properties.")
    versionFile.writeText("version=${project.version}")
    println("version.properties created/updated successfully.")
}

tasks.named("processResources") {
    dependsOn("writeVersion")
}
