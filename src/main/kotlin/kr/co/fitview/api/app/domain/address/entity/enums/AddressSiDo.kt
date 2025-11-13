package kr.co.fitview.api.app.domain.address.entity.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode

enum class AddressSiDo(@get:JsonValue val fullName: String) {
    SEOUL("서울"),
    BUSAN("부산"),
    INCHEON("인천"),
    DAEGU("대구"),
    DAEJEON("대전"),
    GWANGJU("광주"),
    ULSAN("울산"),
    SEJONG("세종"),
    GYEONGGI("경기"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONNAM("전남"),
    JEONBUK("전북"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    GANGWON("강원"),
    JEJU("제주");


    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String): AddressSiDo =
            entries.find { it.fullName == value }
                ?: throw GlobalException(AddressErrorCode.INVALID_SI_DO)

        fun allDescription(): List<String> {
            return AddressSiDo.entries.map { it.fullName }
        }
    }


}