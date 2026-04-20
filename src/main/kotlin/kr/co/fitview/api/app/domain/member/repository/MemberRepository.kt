package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom {

    fun findByEmailAndDeletedAtIsNull(loginId : String) : Member?

    fun findByIdAndDeletedAtIsNull(memberId : Long) : Member?

    fun findByProviderIdAndDeletedAtIsNull(providerId : String) : Member?

    fun findByIdAndDeletedAtIsNotNull(memberId: Long): Member?

    fun existsByNicknameAndDeletedAtIsNull(nickname: String) : Boolean

    fun findByIdAndSignupAtIsNotNullAndDeletedAtIsNull(memberId: Long): Member?

}