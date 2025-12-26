package kr.co.fitview.api.app.docs.fcm

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.fcm.controller.FcmController
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class FcmControllerDocsTest : RestDocsSupport() {

    private val fcmTokenService: FcmTokenService = mock(FcmTokenService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return FcmController(fcmTokenService, securityUtil)
    }

    @DisplayName("fcm 토큰 저장 API")
    @Test
    fun fcmTokenAdd() {
        // given
        val request = FcmTokenCreateRequest(
            deviceId = "dfjaiofjoepfjapiefjoiaejfapefj",
            token = "fcmToken",
            platform = FcmTokenPlatform.ANDROID
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/fcm-tokens")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "fcm-token-add",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("deviceId").type(JsonFieldType.STRING)
                            .description("각 단말기를 구별할 수 있는 고유값. 단말기에서 고유로 얻을 수 있는 값도 괜찮고, FE가 기기에 저장한 UUID도 괜찮음. (의도 : 한 계정으로 여러 단말기 사용 시, 각 단말기마다 fcm 알람을 보내는 용도)"),
                        fieldWithPath("token").type(JsonFieldType.STRING)
                            .description("fcm 토큰"),
                        fieldWithPath("platform").type(JsonFieldType.STRING)
                            .description("안드로이드, 애플 운영체제" + FcmTokenPlatform.allDescriptions()),

                        ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터"),
                    )

                    )
            )
    }


}