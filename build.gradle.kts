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
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val epagesVersion = "0.17.1"
val mockkVersion = "1.13.11"
val kotestVersion = "5.8.1"
val kotlinJdslVersion = "3.5.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.session:spring-session-jdbc") // redis로 변경은 추후 검토
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-client")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.h2database:h2")

    // kotlin jdsl dependencies
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:$kotlinJdslVersion") // JPQL을 만들어 주도록 도와주는 라이브러리
    implementation("com.linecorp.kotlin-jdsl:jpql-render:$kotlinJdslVersion") // DSL로 만든 쿼리를 String으로 변환해주는 라이브러리
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$kotlinJdslVersion") // Spring Data JPA를 지원하는 Kotlin JDSL 라이브러리

    /**
     * https://mvnrepository.com/artifact/com.epages/restdocs-api-spec-mockmvc
     */
    implementation("com.epages:restdocs-api-spec:$epagesVersion")
    implementation("com.epages:restdocs-api-spec-mockmvc:$epagesVersion")
    implementation("com.epages:restdocs-api-spec-openapi3-generator:$epagesVersion")

    implementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.1.0") {
        exclude(group = "com.github.waffle", module = "waffle-jna")
    }
}

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

openapi3 {
    setServer("http://localhost:8080")

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
