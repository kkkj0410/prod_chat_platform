package kr.co.fitview.api.app.docs.fcm

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.fcm.controller.AdminFcmController
import kr.co.fitview.api.app.domain.fcm.controller.FcmController
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmPushRequest
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.dto.response.FcmTokenActiveResponse
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.service.AdminFcmTokenService
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime


class AdminFcmControllerDocsTest : RestDocsSupport() {

    private val adminFcmTokenService: AdminFcmTokenService = mock(AdminFcmTokenService::class.java)

    override fun initController(): Any {
        return AdminFcmController(adminFcmTokenService)
    }

    @DisplayName("활성화된 FCM 토큰 전체 조회")
    @Test
    fun fcmTokenAdd() {
        // given

        val responses = listOf(
            FcmTokenActiveResponse(
                email = "email",
                nickname = "nickname",
                fcmTokenId = 1L,
                memberId = 10L,
                createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
                updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0),
                deviceId = "device-id-1",
                fcmToken = "fcm-token-1",
                platform = FcmTokenPlatform.ANDROID
            ),
            FcmTokenActiveResponse(
                email = "email",
                nickname = "nickname",
                fcmTokenId = 2L,
                memberId = 20L,
                createdAt = LocalDateTime.of(2024, 1, 3, 10, 0),
                updatedAt = LocalDateTime.of(2024, 1, 4, 10, 0),
                deviceId = "device-id-2",
                fcmToken = "fcm-token-2",
                platform = FcmTokenPlatform.IOS
            )
        )

        given(adminFcmTokenService.findActiveFcmTokens())
            .willReturn(responses)

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/fcm-tokens/active")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-fcm-token-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("응답 데이터"),
                        fieldWithPath("data[].email").type(JsonFieldType.STRING)
                            .description("이메일"),
                        fieldWithPath("data[].nickname").type(JsonFieldType.STRING)
                            .description("닉네임"),
                        fieldWithPath("data[].fcmTokenId").type(JsonFieldType.NUMBER)
                            .description("FCM 토큰 ID"),
                        fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER)
                            .description("회원 ID"),
                        fieldWithPath("data[].createdAt").type(JsonFieldType.STRING)
                            .description("생성 시각"),
                        fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING)
                            .description("수정 시각"),
                        fieldWithPath("data[].deviceId").type(JsonFieldType.STRING)
                            .description("디바이스 ID"),
                        fieldWithPath("data[].fcmToken").type(JsonFieldType.STRING)
                            .description("FCM 토큰"),
                        fieldWithPath("data[].platform").type(JsonFieldType.STRING)
                            .description("플랫폼")
                    )

                    )
            )
    }

    @DisplayName("FCM 푸시 전송 (단일 토큰, 프론트 테스트용)")
    @Test
    fun pushFcm() {
        // given
        val request = FcmPushRequest(
            fcmToken = "test-fcm-token",
            platform = FcmTokenPlatform.ANDROID,
            title = "test title",
            body = "test body"
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/admins/fcm-tokens/push")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-fcm-push",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    requestFields(
                        fieldWithPath("fcmToken").type(JsonFieldType.STRING)
                            .description("FCM 토큰"),
                        fieldWithPath("platform").type(JsonFieldType.STRING)
                            .description("플랫폼 (ANDROID / IOS)"),
                        fieldWithPath("title").type(JsonFieldType.STRING)
                            .optional()
                            .description("푸시 제목 (기본값: title)"),
                        fieldWithPath("body").type(JsonFieldType.STRING)
                            .optional()
                            .description("푸시 내용 (기본값: body)")
                    )

                )
            )
    }

}