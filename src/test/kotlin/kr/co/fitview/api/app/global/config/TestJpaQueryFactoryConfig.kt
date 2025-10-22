//package kr.co.fitview.api.app.global.config
//
//import com.querydsl.jpa.impl.JPAQueryFactory
//import jakarta.persistence.EntityManager
//import org.mockito.Mockito.mock
//import org.springframework.boot.test.context.TestConfiguration
//import org.springframework.context.annotation.Bean
//
//
//@TestConfiguration
//class TestJpaQueryFactoryConfig {
//
//    @Bean
//    fun jpaQueryFactory(): JPAQueryFactory {
//        val em: EntityManager = mock(EntityManager::class.java)
//        return JPAQueryFactory(em)
//    }
//}