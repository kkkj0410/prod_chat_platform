package kr.co.fitview.api.app.domain.address.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.Member
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

class AddressTest : IntegrationTestSupport(){


    @DisplayName("주소를 생성한다.")
    @Test
    fun of() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )

        val request = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "해운대구",
            eupMyeonDong = "우동",
            lat = 35.1631,
            lng = 129.1638,
            fullAddress = "부산광역시 해운대구 우동 456-78"
        )

        // when
        val address = Address.of(member, request)

        // then
        assertThat(address)
            .extracting("member", "siDo", "siGunGu")
            .contains(member, request.siDo, request.siGunGu)
    }

//    @DisplayName("주소 생성 시, 시/도가 표준에 맞지 않으면 표준으로 바꿔서 저장한다.")
//    @ParameterizedTest
//    @CsvSource("충청남도, 충청남도", "충남, 충청남도", "충엥남도, 충청남도", "강원도, 강원특별자치도", "강원, 강원특별자치도")
//    fun ofOtherSiDo(siDo : String, standardSiDo : String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
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
//        val address = Address.of(member, request)
//
//        // then
//        assertThat(address)
//            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
//            .contains(standardSiDo, request.siGunGu, request.eupMyeonDong, request.lat, request.lng, request.fullAddress)
//    }
//
//    @DisplayName("회원의 주소 생성 시, 시/도가 한국 특정 시/도 장소로 추정할 수 없으면 저장하지 않는다.")
//    @ParameterizedTest
//    @CsvSource("invalidSiDo", "충청도", "경상도", "전라도", "한국")
//    fun ofInvalidSiDo(siDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
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
//            Address.of(member, request)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(AddressErrorCode.INVALID_SI_DO)
//            })
//    }

    @DisplayName("주소를 수정한다.")
    @Test
    fun update() {
        //given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )

        val request1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "해운대구",
            eupMyeonDong = "우동",
            lat = 35.1631,
            lng = 129.1638,
            fullAddress = "부산광역시 해운대구 우동 456-78"
        )

        val address = Address.of(member, request1)

        val request2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 역삼동 123-45"
        )

        // when
        val modifyAddress = address.update(request2)

        // then
        assertThat(modifyAddress)
            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
            .contains(request2.siDo, request2.siGunGu, request2.eupMyeonDong, request2.lat, request2.lng, request2.fullAddress)

    }


//    @DisplayName("주소 수정 시, 시/도가 표준에 맞지 않으면 표준으로 바꿔서 저장한다.")
//    @ParameterizedTest
//    @CsvSource("충청남도, 충청남도", "충남, 충청남도", "충엥남도, 충청남도", "강원도, 강원특별자치도", "강원, 강원특별자치도")
//    fun updateOtherSiDo(siDo : String, standardSiDo : String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//
//        val request = AddressCreateServiceRequest(
//            siDo = "부산광역시",
//            siGunGu = "해운대구",
//            eupMyeonDong = "우동",
//            lat = 35.1631,
//            lng = 129.1638,
//            fullAddress = "부산광역시 해운대구 우동 456-78"
//        )
//
//        val address = Address.of(member, request)
//
//        val request2 = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "강남구",
//            eupMyeonDong = "역삼동",
//            lat = 37.4979,
//            lng = 127.0276,
//            fullAddress = "서울특별시 강남구 역삼동 123-45"
//        )
//
//        // when
//        val modifyAddress = address.update(request2)
//
//        // then
//        assertThat(modifyAddress)
//            .extracting("siDo", "siGunGu", "eupMyeonDong", "lat", "lng", "fullAddress")
//            .contains(standardSiDo, request2.siGunGu, request2.eupMyeonDong, request2.lat, request2.lng, request2.fullAddress)
//    }
//
//    @DisplayName("회원의 주소 수정 시, 시/도가 한국 특정 시/도 장소로 추정할 수 없으면 저장하지 않는다.")
//    @ParameterizedTest
//    @CsvSource("invalidSiDo", "충청도", "경상도", "전라도", "한국")
//    fun updateInvalidSiDo(siDo: String) {
//        // given
//        val member = Member(
//            email = "email",
//            password = "password",
//            role = Role.USER,
//        )
//
//        val request = AddressCreateServiceRequest(
//            siDo = "부산광역시",
//            siGunGu = "해운대구",
//            eupMyeonDong = "우동",
//            lat = 35.1631,
//            lng = 129.1638,
//            fullAddress = "부산광역시 해운대구 우동 456-78"
//        )
//        val address = Address.of(member, request)
//
//        val request2 = AddressCreateServiceRequest(
//            siDo = siDo,
//            siGunGu = "강남구",
//            eupMyeonDong = "역삼동",
//            lat = 37.4979,
//            lng = 127.0276,
//            fullAddress = "서울특별시 강남구 역삼동 123-45"
//        )
//
//        // when & then
//        assertThatThrownBy {
//            address.update(request2)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(AddressErrorCode.INVALID_SI_DO)
//            })
//    }
}