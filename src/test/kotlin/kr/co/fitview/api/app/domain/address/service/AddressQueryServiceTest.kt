package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AddressQueryServiceTest @Autowired constructor(
    val addressQueryService: AddressQueryService,
    val addressRepository: AddressRepository,
    val memberRepository: MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("회원 id로 주소 조회 시, 주소를 반환한다.")
    @Test
    fun findAddressFromMemberId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val response = addressQueryService.findAddressFromMemberId(member.id!!)

        // then
        assertThat(response)
            .extracting("addressId", "siDo", "siGunGu", "eupMyeonDong")
            .contains(savedAddress.id, savedAddress.siDo, savedAddress.siGunGu, savedAddress.eupMyeonDong)
    }

    @DisplayName("회원 id로 주소 조회 시, 회원이 주소가 없다면 주소 조회에 실패한다.")
    @Test
    fun findAddressFromMemberIdWithoutAddressMemberId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when & then
        assertThatThrownBy {
            addressQueryService.findAddressFromMemberId(member.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
            })
    }

    @DisplayName("회원 id로 주소 조회 시, 서울 외 지역 사람은 서울 기본 주소로 조회한다.")
    @Test
    fun findAddressFromMemberIdNotSeoul() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.JEJU,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val response = addressQueryService.findAddressFromMemberId(member.id!!)

        // then
        assertThat(response)
            .extracting("addressId", "siDo", "siGunGu", "eupMyeonDong")
            .contains(savedAddress.id,
                AddressConstant.DEFAULT_SIDO,
                AddressConstant.DEFAULT_SIGUNGU,
                AddressConstant.DEFAULT_EUPMYEONDONG
            )

    }

    @DisplayName("주소 id로 주소 조회 시, 주소를 반환한다.")
    @Test
    fun findAddressFromAddressId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val response = addressQueryService.findAddressFromAddressId(savedAddress.id!!)

        // then
        assertThat(response)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng")
            .contains(
                savedAddress.siDo,
                savedAddress.siGunGu,
                savedAddress.eupMyeonDong,
                savedAddress.lat,
                savedAddress.lng
            )
    }

    @DisplayName("주소 id로 주소 조회 시, 주소가 없다면 주소 조회에 실패한다.")
    @Test
    fun findAddressFromAddressIdWithoutAddressMemberId() {
        // given

        // when & then
        assertThatThrownBy {
            addressQueryService.findAddressFromAddressId(1L)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.ADDRESS_NOT_FOUND)
            })
    }

    @DisplayName("주소 id로 주소 조회 시, 서울 외 지역 사람은 기본 서울 주소로 반환한다.")
    @Test
    fun findAddressFromAddressIdNotSeoul() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.INCHEON,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        val savedAddress = addressRepository.save(address)

        // when
        val response = addressQueryService.findAddressFromAddressId(savedAddress.id!!)

        // then
        assertThat(response)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng")
            .contains(
                AddressConstant.DEFAULT_SIDO,
                AddressConstant.DEFAULT_SIGUNGU,
                AddressConstant.DEFAULT_EUPMYEONDONG,
                AddressConstant.DEFAULT_LAT,
                AddressConstant.DEFAULT_LNG,
            )

    }

    @DisplayName("회원의 주소를 조회한다.")
    @Test
    fun findAddressEntityFrom() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val address = Address(
            member = member,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "테헤란로",
            lat = 10.123,
            lng = 10.234,
            fullAddress = "fullAddress"
        )
        addressRepository.save(address)

        // when
        val findAddress = addressQueryService.findAddressEntityFrom(member.id!!)

        // then
        assertThat(findAddress)
            .extracting("member", "siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
            .contains(
                member,
                address.siDo,
                address.siGunGu,
                address.eupMyeonDong,
                address.lat,
                address.lng,
                address.fullAddress,
            )

    }
}
