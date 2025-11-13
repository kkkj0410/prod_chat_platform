package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.math.cos


class MemberRepositoryTest@Autowired constructor(
    val memberRepository : MemberRepository,
    val addressRepository: AddressRepository
) : IntegrationTestSupport() {


    @DisplayName("로그인 id로 해당 회원을 조회한다.")
    @Test
    fun findByEmailAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(savedMember.email!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
    }

    @DisplayName("저장되지 않은 회원은 로그인 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByEmailAndDeletedAtIsNullWithoutMember() {
        // given
        val loginId = "loginId"

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(loginId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
    }


    @DisplayName("저장되지 않은 회원은 회원 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByIdAndDeletedAtIsNullWithoutMember() {
        // given
        val memberId = 100L

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(memberId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("소셜 로그인 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)

    }


    @DisplayName("저장되지 않은 회원은 소셜 로그인 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNullWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)

        // then
        assertThat(findMember).isNull()
    }

    private fun createAddress(
        lat : Double,
        lng : Double,
        radiusKm : Double = 5.0
    ) : Address{
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = lat,
            lng = lng,
            fullAddress = "fullAddress",
            radiusKm = radiusKm
        )
        return addressRepository.save(address)
    }

    private fun createBoundingBox(address: Address) : BoundingBox {
        val latDeg = address.radiusKm?.div(111)
        val latRad = Math.toRadians(address.lat!!)
        val lngDeg = address.radiusKm?.div(111 * cos(latRad))

        val minLat = address.lat?.minus(latDeg!!)
        val maxLat = address.lat?.plus(latDeg!!)

        val minLng = address.lng?.minus(lngDeg!!)
        val maxLng = address.lng?.plus(lngDeg!!)

        return BoundingBox(
            minLat = minLat!!,
            maxLat = maxLat!!,
            minLng = minLng!!,
            maxLng = maxLng!!
        )
    }

    data class BoundingBox(
        val minLat : Double,
        val maxLat : Double,
        val minLng : Double,
        val maxLng : Double
    )

    @DisplayName("현재 회원 인근에 존재하는 회원을 최대 100명 조회한다.")
    @Test
    fun findMemberWithinLocal() {
        // given
        val baseAddress = createAddress(
            lat = 50.0,
            lng = 50.0,
            radiusKm = 10.0
        )
        val baseMember = baseAddress.member

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val boundingBox = createBoundingBox(baseAddress)

        val savedAddress1 = createAddress(
            lat = 49.9,
            lng = 50.14
        )
        val savedAddress2 = createAddress(
            lat = 49.5,
            lng = 50.0
        )
        val savedAddress3 = createAddress(
            lat = 49.8,
            lng = 49.84
        )
        val savedAddress4 = createAddress(
            lat = 50.1,
            lng = 50.15
        )

        val condition = MemberLocalCondition()

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember!!.id!!, condition)

        // then
//        assertThat(response)
//            .extracting("member", "image", "type")
//            .containsExactlyInAnyOrder(
//                tuple(savedMember, savedImage1, MemberImageType.WORKOUT),
//                tuple(savedMember, savedImage2, MemberImageType.WORKOUT),
//            )
    }



}