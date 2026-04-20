package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
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

class AddressServiceTest @Autowired constructor(
    val addressService: AddressService,
    val addressRepository: AddressRepository,
    val memberRepository: MemberRepository
) : IntegrationTestSupport() {


    @DisplayName("회원 주소를 추가한다.")
    @Test
    fun addAddress() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 123.123,
            lng = 987.987,
            fullAddress = "fullAddress"
        )

        // when
        val savedAddress = addressService.addAddress(member, request)

        // then
        assertThat(savedAddress.id).isNotNull()
        assertThat(savedAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
            .contains(
                request.siDo,
                request.siGunGu,
                request.eupMyeonDong,
                request.lat,
                request.lng,
                request.fullAddress
            )
    }

    @DisplayName("회원에게 이미 주소가 있다면, 해당 주소를 삭제하고 새로 만든다.")
    @Test
    fun addAddressExistingAddress() {
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
        addressRepository.save(address)

        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 123.123,
            lng = 987.987,
            fullAddress = "fullAddress"
        )

        // when
        val savedAddress = addressService.addAddress(member, request)

        // then
        assertThat(savedAddress.id).isNotNull()
        assertThat(savedAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
            .contains(
                request.siDo,
                request.siGunGu,
                request.eupMyeonDong,
                request.lat,
                request.lng,
                request.fullAddress
            )
    }

    @DisplayName("주소를 변경한다.")
    @Test
    fun modifyAddress() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

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

        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "해운대구",
            eupMyeonDong = "우동",
            lat = 35.1631,
            lng = 129.1638,
            fullAddress = "부산광역시 해운대구 우동 456-78"
        )

        // when
        val modifyAddress = addressService.modifyAddress(savedMember.id!!, savedAddress.id!!, request)

        // then
        assertThat(modifyAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
            .contains(
                request.siDo,
                request.siGunGu,
                request.eupMyeonDong,
                request.lat,
                request.lng,
                request.fullAddress
            )
    }

    @DisplayName("주소가 없다면 주소를 변경할 수 없다.")
    @Test
    fun modifyAddressWithoutAddress() {
        // given
        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "해운대구",
            eupMyeonDong = "우동",
            lat = 35.1631,
            lng = 129.1638,
            fullAddress = "부산광역시 해운대구 우동 456-78"
        )

        // when & then
        assertThatThrownBy {
            addressService.modifyAddress(1L, 1L, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
            })

    }

    @DisplayName("해당 회원의 주소가 아니면 해당 주소를 변경할 수 없다.")
    @Test
    fun modifyAddressInvalidMemberId() {
        // given
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email2",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)
        val savedOtherMember = memberRepository.save(otherMember)

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

        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "해운대구",
            eupMyeonDong = "우동",
            lat = 35.1631,
            lng = 129.1638,
            fullAddress = "부산광역시 해운대구 우동 456-78"
        )

        // when & then
        assertThatThrownBy {
            addressService.modifyAddress(savedOtherMember.id!!, savedAddress.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
            })

    }
}
