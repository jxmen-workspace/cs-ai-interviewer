import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("com.epages.restdocs-api-spec") version "0.17.1"
}

group = "dev.jxmen"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val epagesVersion = "0.17.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2")

    /**
     * https://mvnrepository.com/artifact/com.epages/restdocs-api-spec-mockmvc
     */
    implementation("com.epages:restdocs-api-spec:$epagesVersion")
    implementation("com.epages:restdocs-api-spec-mockmvc:$epagesVersion")
    implementation("com.epages:restdocs-api-spec-openapi3-generator:$epagesVersion")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    finalizedBy("openapi3")
    finalizedBy("copyOasToSwagger")
}

tasks.jar {
    enabled = false // plain jar 파일 생성 비활성화
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

openapi3 {
    setServer("http://localhost:8080")

    format = "yaml"
}

tasks.register<Copy>("copyOasToSwagger") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "openapi 파일 정적 경로에 복사"

    delete("src/main/resources/static/swagger-ui/openapi3.yaml") // 기존 OAS 파일 삭제
    from(layout.buildDirectory.file("api-spec/openapi3.yaml")) // 복제할 OAS 파일 지정
    into("src/main/resources/static/swagger-ui/.") // 타겟 디렉터리로 파일 복제

    dependsOn("openapi3") // openapi3 Task가 먼저 실행되도록 설정
}
