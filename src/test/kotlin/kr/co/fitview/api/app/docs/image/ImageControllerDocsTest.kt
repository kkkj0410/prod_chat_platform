package kr.co.fitview.api.app.docs.image

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.image.controller.ImageController
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlRequest
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.domain.image.enums.S3Prefix
import kr.co.fitview.api.app.domain.image.service.S3Service
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.exception.error.security.SecurityErrorCode
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.cookies.CookieDocumentation.responseCookies
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status




class ImageControllerDocsTest : RestDocsSupport() {

    private val s3Service: S3Service = mock(S3Service::class.java)

    override fun initController(): Any {
        return ImageController(s3Service)
    }

    @DisplayName("이미지 presigned url 발급 API")
    @Test
    fun presign() {
        // given
        val requests = listOf(
            S3UploadUrlRequest(
                prefix = S3Prefix.MEMBER_PROFILE,
                imageByte = 100
            )
        )

        given(s3Service.getAllUploadUrls(any()))
            .willReturn(
                listOf(S3UploadUrlResponse(
                    presignedUrl = "presignedUrl",
                    accessUrl = "accessUrl"
                )
            )
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/images/presign")
            .header("Authorization", "Bearer jwt-token")
            .content(objectMapper.writeValueAsString(requests))
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("image-presign",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                RestDocsHeaders.authorizationHeader(Role.USER)
            ),

            requestFields(
                fieldWithPath("[].prefix").type(JsonFieldType.STRING)
                    .description(S3Prefix.allDescriptions()),
                fieldWithPath("[].imageByte").type(JsonFieldType.NUMBER)
                    .description("업로드 사진의 byte 용량(무지막지하게 큰 용량의 파일 업로드를 막는 용도(원인 : 인프라에서 용량 업로드 제한 불가 문제)). 조건1 : 용량은 50MB 이하. 조건2 : 업로드 사진의 byte 용량과 일치하지 않으면 업로드 불가. 조건3 : 50개 초과하는 이미지 업로드는 불가"),
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
                fieldWithPath("data[].presignedUrl").type(JsonFieldType.STRING)
                    .description("이미지 업로드 url"),
                fieldWithPath("data[].accessUrl").type(JsonFieldType.STRING)
                    .description("presignedUrl에 이미지 업로드 완료 시, 해당 url에서 이미지 조회 가능")
            )
            ))
    }


}