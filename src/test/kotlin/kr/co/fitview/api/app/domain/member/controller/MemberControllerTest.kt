package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class MemberControllerTest : ControllerTestSupport(){

//    private fun setMemberFromSecurity() {
//        val userPrincipal = UserPrincipal(1L, Role.USER)
//        val newAuthentication = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
//        SecurityContextHolder.getContext().authentication = newAuthentication
//    }


    @DisplayName("인증된 jwt 토큰으로 회원 정보를 조회한다.")
    @Test
    fun memberMe() {
        // given
//        setMemberFromSecurity()

        // when // then
        mockMvc.perform(
            get("/api/v1/members/me")
            .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @DisplayName("회원 본인을 삭제한다.")
    @Test
    fun memberRemove() {

        // when & then
        mockMvc.perform(
            delete("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")

        )
            .andDo(print())
            .andExpect(status().isOk())

            responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER),
                fieldWithPath("code").type(JsonFieldType.STRING),
                fieldWithPath("message").type(JsonFieldType.STRING),
                fieldWithPath("data").type(JsonFieldType.STRING)
            )

    }

    @DisplayName("회원 id로 해당 회원의 주소를 조회한다.")
    @Test
    fun memberAddressDetails() {
        given(addressService.findAddressFromMemberId(any()))
            .willReturn(
                AddressResponse(
                    addressId = 1L,
                    siDo = "서울특별시",
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동"
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/members/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER),
                fieldWithPath("code").type(JsonFieldType.STRING),
                fieldWithPath("message").type(JsonFieldType.STRING),
                fieldWithPath("data").type(JsonFieldType.OBJECT),
                fieldWithPath("data.addressId").type(JsonFieldType.NUMBER),
                fieldWithPath("data.siDo").type(JsonFieldType.STRING),
                fieldWithPath("data.siGunGu").type(JsonFieldType.STRING),
                fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING)
            )
    }

//    @DisplayName("인증된 jwt 토큰이 없다면 회원 정보를 조회하지 못한다.")
//    @Test
//    fun memberMeWithoutAuthentication() {
//
//        // when // then
//        mockMvc.perform(
//            get("/api/v1/members/me")
//                .header("Authorization", "Bearer jwt-token")
//        )
//            .andDo(print())
//            .andExpect(status().isUnauthorized())
//            .andExpect(jsonPath("$.code").value(JwtErrorCode.JWT_TOKEN_INVALID.code))
//            .andExpect(jsonPath("$.status").value("401"))
//            .andExpect(jsonPath("$.message").value(JwtErrorCode.JWT_TOKEN_INVALID.message))
//            .andExpect(jsonPath("$.data").isEmpty())
//    }

}