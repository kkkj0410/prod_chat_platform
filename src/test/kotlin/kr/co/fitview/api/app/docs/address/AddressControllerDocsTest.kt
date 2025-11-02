package kr.co.fitview.api.app.docs.address

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.controller.AddressController
import kr.co.fitview.api.app.domain.address.dto.request.AddressRadiusRequest
import kr.co.fitview.api.app.domain.address.dto.request.AddressUpdateRequest
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant

import kr.co.fitview.api.app.global.entity.Role
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters


class AddressControllerDocsTest : RestDocsSupport() {

    private val addressService: AddressService = mock(AddressService::class.java)

    override fun initController(): Any {
        return AddressController(addressService)
    }

    @DisplayName("특정 주소 조회 API")
    @Test
    fun addressDetail() {
        // when // then
        mockMvc.perform(
            get("/api/v1/addresses/{addressId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("address-detail",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                RestDocsHeaders.authorizationHeader(Role.USER)
            ),

            pathParameters(
                parameterWithName("addressId").description("조회할 주소 id")
            ),

            responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER)
                    .description("상태"),
                fieldWithPath("code").type(JsonFieldType.STRING)
                    .description("코드"),
                fieldWithPath("message").type(JsonFieldType.STRING)
                    .description("에러 메시지"),
                fieldWithPath("data").type(JsonFieldType.OBJECT)
                    .description("응답 데이터"),
                fieldWithPath("data.fullAddress").type(JsonFieldType.STRING)
                    .description("회원 전체 주소. 서울 외 지역 사람은 서울시 서초구 서초동으로 반환"),
                fieldWithPath("data.lat").type(JsonFieldType.NUMBER)
                    .description("위도. 서울 외 지역 사람은 37.4900861966502"),
                fieldWithPath("data.lng").type(JsonFieldType.NUMBER)
                    .description("경도. 서울 외 지역 사람은 127.01953478052"),
                )
            ))
    }


    @DisplayName("회원 전체 주소 변경")
    @Test
    fun addressModify() {
        // given
        val request = AddressUpdateRequest(
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4995539438207,
            lng = 127.031393491745,
            fullAddress = "서울 강남구 테헤란로 123",
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/addresses/{addressId}", 1)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("address-update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                RestDocsHeaders.authorizationHeader(Role.USER)
            ),

            pathParameters(
                parameterWithName("addressId").description("수정 대상 주소 id")
            ),

            requestFields(
                fieldWithPath("siDo").type(JsonFieldType.STRING).optional()
                    .description("시/도 (입력값에 반드시 포함되어야 하는 키워드: 서울, 부산, 인천, 대구, 대전, 광주, 울산, 세종, 경기, 충북, 충남, 전남, 전북, 경북, 경남, 강원, 제주) " +
                            "ex)충청남도 -> 충청남도, 충남 -> 충청남도, 충엥남도 -> 충청남도, 강원도 -> 강원특별자치도, 강원 -> 강원특별자치도 로 변환해서 저장"),
                fieldWithPath("siGunGu").type(JsonFieldType.STRING).optional()
                    .description("시/군/구"),
                fieldWithPath("eupMyeonDong").type(JsonFieldType.STRING).optional()
                    .description("읍/면/동"),
                fieldWithPath("postalCode").type(JsonFieldType.STRING).optional()
                    .description("우편번호"),
                fieldWithPath("lat").type(JsonFieldType.NUMBER)
                    .description("위도"),
                fieldWithPath("lng").type(JsonFieldType.NUMBER)
                    .description("경도"),
                fieldWithPath("fullAddress").type(JsonFieldType.STRING).optional()
                    .description("전체 주소. 상세 주소(oo아파트 몇호) 제외. 법정동/행정동/도로명 기반 구분없이 전체 주소 받음"),
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


    @DisplayName("회원 주소 반경 변경 API")
    @Test
    fun addressRadiusModify() {
        // given
        val request = AddressRadiusRequest(
            radius = 2
        )

        // when // then
        mockMvc.perform(
            patch("/api/v1/addresses/{addressId}/radius", 1)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("address-radius-update",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                pathParameters(
                    parameterWithName("addressId").description("수정 대상 주소 id")
                ),

                requestFields(
                    fieldWithPath("radius").type(JsonFieldType.NUMBER)
                        .description("해당 주소 기준으로 반경 km ex) radius = 5 -> 반경 5km")
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