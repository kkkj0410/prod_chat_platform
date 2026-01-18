package kr.co.fitview.api.app.domain.fcm.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FcmTokenTest : IntegrationTestSupport(){


    @DisplayName("기기 고유 번호를 업데이트 한다")
    @Test
    fun updateToken() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )

        val otherMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )

        val fcmToken = FcmToken.of(
            member = member,
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )
        // when
        fcmToken.updateToken(otherMember, "updateToken")

        // then
        assertThat(fcmToken.token).isEqualTo("updateToken")
        assertThat(fcmToken.member).isEqualTo(otherMember)

    }
}