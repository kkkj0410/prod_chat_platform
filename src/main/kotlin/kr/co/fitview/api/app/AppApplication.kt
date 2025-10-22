package kr.co.fitview.api.app

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@SpringBootApplication
class AppApplication{
}

fun main(args: Array<String>) {
	runApplication<AppApplication>(*args)

}
