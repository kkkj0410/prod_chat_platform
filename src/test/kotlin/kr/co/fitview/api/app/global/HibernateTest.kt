//package kr.co.fitview.api.app.global
//
//import jakarta.persistence.EntityManagerFactory
//import kr.co.fitview.api.app.IntegrationTestSupport
//import kr.co.fitview.api.app.domain.member.entity.Member
//import kr.co.fitview.api.app.domain.member.repository.MemberRepository
//import kr.co.fitview.api.app.global.entity.Role
//import org.hibernate.SessionFactory
//import org.hibernate.internal.SessionImpl
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import java.lang.reflect.Field
//
//
//class HibernateTest @Autowired constructor(
//    val entityManagerFactory : EntityManagerFactory,
//    val memberRepository : MemberRepository
//) : IntegrationTestSupport(){
//
//    // 테스트용 데이터 미리 세팅
//    @BeforeEach
//    fun setUp() {
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        val savedMember = memberRepository.save(member)
//
//        // Post 5개 생성
//        for (i in 0..4) {
//            val member = Member(
//                email = "email${i}",
//                password = "password",
//                role = Role.USER,
//            )
//            memberRepository.save(member)
//        }
//    }
//
//    @DisplayName("")
//    @Test
//    fun test() {
//
//        // set
//        val sessionFactory: SessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
//        val statistics = sessionFactory.statistics
//        statistics.isStatisticsEnabled = true
//        statistics.clear()
//
//        // query
//        memberRepository.findAll()
//        memberRepository.findAll()
//        memberRepository.findAll()
//
//
//        // result
//        val queryCount = statistics.queryExecutionCount
//        println("==================================")
//        println("총 실행된 쿼리 수: $queryCount")
//        println("==================================")
//    }
//
//    @DisplayName("")
//    @Test
//    fun uniqueKey() {
////        val em = entityManagerFactory.createEntityManager()
////        em.transaction.begin()
////
////        val member = Member(
////                email = "email",
////                password = "password",
////                role = Role.USER,
////            )
////        em.persist(member)
////
////        em.flush() // flush 전에 캐시 저장됨
////
////
////// Session 객체 가져오기
////        val session = em.unwrap(SessionImpl::class.java)
////
////
////// Reflection으로 private 필드 접근
////        val field : Field = SessionImpl::class.java.getDeclaredField("entitiesByUniqueKey")
////        field.setAccessible(true)
////
////        val entitiesByUniqueKey = field[session] as Map<*, *>
////
////        entitiesByUniqueKey.forEach { (k, v) ->
////            println("Key: $k")
////            println("Value: $v")
////        }
////
////
////        em.transaction.commit()
////        em.close()
//
//        val em = entityManagerFactory.createEntityManager()
//        em.transaction.begin()
//
//        val member = Member("email", "pw", Role.USER)
//        em.persist(member)
//        em.flush()
//
//// Session 가져오기
//        val session = em.unwrap(SessionImpl::class.java)
//
//// PersistenceContext 가져오기
//        val pcField = SessionImpl::class.java.getDeclaredField("persistenceContext")
//        pcField.isAccessible = true
//        val pc = pcField.get(session)
//
//// entityEntries 가져오기
//        val entriesField = pc.javaClass.getDeclaredField("entityEntries")
//        entriesField.isAccessible = true
//        val entityEntries = entriesField.get(pc) as Map<*, *>
//
//        entityEntries.forEach { (k, v) ->
//            println("Key: $k")
//            println("Value: $v")
//        }
//
//        em.transaction.commit()
//        em.close()
//
//    }
//}