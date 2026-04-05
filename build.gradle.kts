plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("kapt") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	id ("org.asciidoctor.jvm.convert") version "4.0.5"
}

if (project.hasProperty("local")) {
	tasks.withType<Test>().configureEach {
		isEnabled = false
	}
}

group = "kr.co.fitview.api"
version = "0.0.1-SNAPSHOT"
description = "fitview 2nd app"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

val asciidoctorExt by configurations.creating

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	compileOnly("org.projectlombok:lombok")

	runtimeOnly ("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// RestDocs
	asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

	// jwt
	implementation("io.jsonwebtoken:jjwt:0.12.6")
	implementation ("com.auth0:java-jwt:4.2.1")

	// apple oauth2 jwt validate
	implementation ("com.nimbusds:nimbus-jose-jwt:10.3")

	//thymeleaf
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	//mock
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

	//websocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	//webClient
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	//aws
	implementation("io.awspring.cloud:spring-cloud-aws-starter:3.2.1")
	implementation("software.amazon.awssdk:s3:2.32.22")

	//querydsl
	implementation ("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

	// java time/datetime
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	// spring security - WebSocket
	implementation("org.springframework.security:spring-security-messaging")

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	//firebase
	implementation ("com.google.firebase:firebase-admin:9.2.0")

	//shed-lock
	implementation("net.javacrumbs.shedlock:shedlock-spring:5.13.0")
	implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.13.0")
	testImplementation("net.javacrumbs.shedlock:shedlock-provider-inmemory:7.2.1")

	//프로메테우스
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("io.micrometer:micrometer-registry-prometheus")

	//sqids - 회원 id -> 코드 변환 용도
	implementation("org.sqids:sqids:0.1.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}


// 1. 전역 변수 (Groovy ext 대신 Kotlin val)
val snippetsDir = file("build/generated-snippets")

// 2. test task
tasks.test {
	outputs.dir(snippetsDir)

	// 테스트 병렬 실행
	maxParallelForks = 4

	forkEvery = 100

}

// 3. asciidoctor task
tasks.named<org.asciidoctor.gradle.jvm.AsciidoctorTask>("asciidoctor") {
	inputs.dir(snippetsDir)
	configurations("asciidoctorExt")

	sources {
		include("**/index.adoc", "**/exception.adoc")
	}
	baseDirFollowsSourceFile() // 다른 adoc 파일 include 시, 경로를 baseDir로 맞추기
	dependsOn(tasks.test)
}

// 4. bootJar task
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	dependsOn(tasks.named("asciidoctor"))
	from(tasks.named<org.asciidoctor.gradle.jvm.AsciidoctorTask>("asciidoctor").get().outputDir) {
		into("static/docs")
	}
}