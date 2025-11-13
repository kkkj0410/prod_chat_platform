package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.dto.request.AddressRadiusServiceRequest
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

//    @DisplayName("회원의 주소 저장 시, 시/도가 표준에 맞지 않으면 표준으로 바꿔서 저장한다.")
//    @ParameterizedTest
//    @CsvSource("충청남도, 충청남도", "충남, 충청남도", "충엥남도, 충청남도", "강원도, 강원특별자치도", "강원, 강원특별자치도")
//    fun addAddressOtherSiDo(siDo: String, standardSiDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        memberRepository.save(member)
//
//        val request = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "강남구",
//            eupMyeonDong = "역삼동",
//            lat = 123.123,
//            lng = 987.987,
//            fullAddress = "fullAddress",
//        )
//
//        // when
//        val savedAddress = addressService.addAddress(member, request)
//
//        // then
//        assertThat(savedAddress.id).isNotNull()
//        assertThat(savedAddress)
//            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
//            .contains(
//                standardSiDo,
//                request.siGunGu,
//                request.eupMyeonDong,
//                request.lat,
//                request.lng,
//                request.fullAddress
//            )
//    }
//
//    @DisplayName("회원의 주소 저장 시, 시/도가 한국 특정 시/도 장소로 추정할 수 없으면 저장하지 않는다.")
//    @ParameterizedTest
//    @CsvSource("invalidSiDo", "충청도", "경상도", "전라도", "한국")
//    fun addAddressInvalidSiDo(siDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        memberRepository.save(member)
//
//        val request = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "강남구",
//            eupMyeonDong = "역삼동",
//            lat = 123.123,
//            lng = 987.987,
//            fullAddress = "fullAddress"
//        )
//
//        // when & then
//        assertThatThrownBy {
//            addressService.addAddress(member, request)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(AddressErrorCode.INVALID_SI_DO)
//            })
//    }


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
        val response = addressService.findAddressFromMemberId(member.id!!)

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
            addressService.findAddressFromMemberId(member.id!!)
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
        val response = addressService.findAddressFromMemberId(member.id!!)

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
        val response = addressService.findAddressFromAddressId(savedAddress.id!!)

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
            addressService.findAddressFromAddressId(1L)
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
        val response = addressService.findAddressFromAddressId(savedAddress.id!!)

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

//    @DisplayName("회원의 주소 수정 시, 시/도가 표준에 맞지 않으면 표준으로 바꿔서 저장한다.")
//    @ParameterizedTest
//    @CsvSource("충청남도, 충청남도", "충남, 충청남도", "충엥남도, 충청남도", "강원도, 강원특별자치도", "강원, 강원특별자치도")
//    fun modifyAddressOtherSiDo(siDo: String, standardSiDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        val savedMember = memberRepository.save(member)
//
//        val address = Address(
//            member = member,
//            siDo = "서울특별시",
//            siGunGu = "강남구",
//            eupMyeonDong = "테헤란로",
//            lat = 10.123,
//            lng = 10.234,
//            fullAddress = "fullAddress"
//        )
//        val savedAddress = addressRepository.save(address)
//
//        val request = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "해운대구",
//            eupMyeonDong = "우동",
//            lat = 35.1631,
//            lng = 129.1638,
//            fullAddress = "부산광역시 해운대구 우동 456-78"
//        )
//
//        // when
//        val modifyAddress = addressService.modifyAddress(savedMember.id!!, savedAddress.id!!, request)
//
//        // then
//        assertThat(modifyAddress.id).isNotNull()
//        assertThat(modifyAddress)
//            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
//            .contains(
//                standardSiDo,
//                request.siGunGu,
//                request.eupMyeonDong,
//                request.lat,
//                request.lng,
//                request.fullAddress
//            )
//    }
//
//    @DisplayName("회원의 주소 수정 시, 시/도가 한국 특정 시/도 장소로 추정할 수 없으면 저장하지 않는다.")
//    @ParameterizedTest
//    @CsvSource("invalidSiDo", "충청도", "경상도", "전라도", "한국")
//    fun modifyAddressInvalidSiDo(siDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//        val savedMember = memberRepository.save(member)
//
//        val address = Address(
//            member = member,
//            siDo = "서울특별시",
//            siGunGu = "강남구",
//            eupMyeonDong = "테헤란로",
//            lat = 10.123,
//            lng = 10.234,
//            fullAddress = "fullAddress"
//        )
//        val savedAddress = addressRepository.save(address)
//
//        val request = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "해운대구",
//            eupMyeonDong = "우동",
//            lat = 35.1631,
//            lng = 129.1638,
//            fullAddress = "부산광역시 해운대구 우동 456-78"
//        )
//
//        // when & then
//        assertThatThrownBy {
//            addressService.modifyAddress(savedMember.id!!, savedAddress.id!!, request)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(AddressErrorCode.INVALID_SI_DO)
//            })
//    }

    @DisplayName("특정 주소의 탐색 반경을 수정한다.")
    @Test
    fun modifyRadiusKm() {
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

        val request = AddressRadiusServiceRequest(
            radiusKm = 10
        )

        // when
        val modifyAddress = addressService.modifyRadiusKm(savedMember.id!!, savedAddress.id!!, request)

        // then
        assertThat(modifyAddress.radiusKm).isEqualTo(request.radiusKm.toDouble())
    }

    @DisplayName("주소의 탐색 반경은 음수가 될 수 없다.")
    @Test
    fun modifyRadiusKmNegativeNumber() {
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

        val request = AddressRadiusServiceRequest(
            radiusKm = -1
        )

        // when & then
        assertThatThrownBy {
            addressService.modifyRadiusKm(savedMember.id!!, savedAddress.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.NEGATIVE_RADIUS)
            })

    }


    @DisplayName("주소가 없다면 탐색 반경을 변경할 수 없다.")
    @Test
    fun modifyRadiusKmWithoutAddress() {
        // given
        val request = AddressRadiusServiceRequest(
            radiusKm = 10
        )

        // when & then
        assertThatThrownBy {
            addressService.modifyRadiusKm(1L, 1L, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
            })

    }

    @DisplayName("해당 회원의 주소가 아니면 해당 주소의 탐색 반경을 변경할 수 없다.")
    @Test
    fun modifyRadiusKmInvalidMemberId() {
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

        val request = AddressRadiusServiceRequest(
            radiusKm = 10
        )

        // when & then
        assertThatThrownBy {
            addressService.modifyRadiusKm(savedOtherMember.id!!, savedAddress.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
            })

    }


}