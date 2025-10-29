package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.Address
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

class AddressServiceTest @Autowired constructor(
    val addressService: AddressService,
    val addressRepository: AddressRepository,
    val memberRepository : MemberRepository
) : IntegrationTestSupport(){


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
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = "roadAddress",
            inputAddress = null
        )

        // when
        val savedAddress = addressService.addAddress(member, request)

        // then
        assertThat(savedAddress.id).isNotNull()
        assertThat(savedAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "postalCode", "lat", "lng", "roadAddress")
            .contains(request.siDo, request.siGunGu, request.eupMyeonDong, request.postalCode, request.lat, request.lng, request.roadAddress)
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
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "테헤란로",
            postalCode = "1234",
            lat = 10.123,
            lng = 10.234,
            roadAddress = "roadAddress"
        )
        addressRepository.save(address)

        val request = AddressCreateServiceRequest(
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = "roadAddress",
            inputAddress = null
        )

        // when
        val savedAddress = addressService.addAddress(member, request)

        // then
        assertThat(savedAddress.id).isNotNull()
        assertThat(savedAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "postalCode", "lat", "lng", "roadAddress")
            .contains(request.siDo, request.siGunGu, request.eupMyeonDong, request.postalCode, request.lat, request.lng, request.roadAddress)
    }

    @DisplayName("회원의 주소 저장 시, 시/도가 표준에 맞지 않으면 표준으로 바꿔서 저장한다.")
    @ParameterizedTest
    @CsvSource("충청남도, 충청남도", "충남, 충청남도", "충엥남도, 충청남도", "강원도, 강원특별자치도", "강원, 강원특별자치도")
    fun addAddressOtherSiDo(siDo : String, standardSiDo : String) {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = "roadAddress",
            inputAddress = null
        )

        // when
        val savedAddress = addressService.addAddress(member, request)

        // then
        assertThat(savedAddress.id).isNotNull()
        assertThat(savedAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "postalCode", "lat", "lng", "roadAddress")
            .contains(standardSiDo, request.siGunGu, request.eupMyeonDong, request.postalCode, request.lat, request.lng, request.roadAddress)
    }

    @DisplayName("회원의 주소 저장 시, 시/도가 한국 특정 시/도 장소로 추정할 수 없으면 저장하지 않는다.")
    @ParameterizedTest
    @CsvSource("invalidSiDo", "충청도", "경상도", "전라도", "한국")
    fun addAddressInvalidSiDo(siDo: String) {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = "roadAddress",
            inputAddress = null
        )

        // when & then
        assertThatThrownBy {
            addressService.addAddress(member, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.INVALID_SI_DO)
            })
    }

    @DisplayName("회원의 주소 저장 시, 도로명 주소와 사용자 입력 주소 모두 비어있으면 안된다")
    @Test
    fun addAddressRoadAddressOrInputAddress() {

        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = AddressCreateServiceRequest(
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = null,
            inputAddress = null
        )

        // when & then
        assertThatThrownBy {
            addressService.addAddress(member, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.INPUT_AND_ROAD_ADDRESS_BOTH_NULL)
            })
    }

    @DisplayName("회원의 주소 저장 시, 도로명 주소와 사용자 입력 주소가 모두 비어있으면 안된다.")
    @Test
    fun addAddressIsNullRoadAddressAndInputAddress() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = AddressCreateServiceRequest(
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            postalCode = "1234",
            lat = 123.123,
            lng = 987.987,
            roadAddress = "roadAddress",
            inputAddress = "inputAddress"
        )

        // when & then
        assertThatThrownBy {
            addressService.addAddress(member, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.INPUT_AND_ROAD_ADDRESS_BOTH_PRESENT)
            })
    }



}