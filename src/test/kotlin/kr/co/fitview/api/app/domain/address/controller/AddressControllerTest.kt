package kr.co.fitview.api.app.domain.address.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressUpdateRequest
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.member.entity.Member
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
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AddressControllerTest : ControllerTestSupport(){

    @DisplayName("주소 id로 해당 주소를 조회한다.")
    @Test
    fun addressDetail() {
        given(addressService.findAddressFromAddressId(any()))
            .willReturn(
                AddressDetailResponse(
                    siDo = "서울특별시",
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동",
                    lat = 37.4995539438207,
                    lng = 127.031393491745
                )
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/addresses/{addressId}", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER),
                fieldWithPath("code").type(JsonFieldType.STRING),
                fieldWithPath("message").type(JsonFieldType.STRING),
                fieldWithPath("data").type(JsonFieldType.OBJECT),
                fieldWithPath("data.siDo").type(JsonFieldType.STRING),
                fieldWithPath("data.siGunGu").type(JsonFieldType.STRING),
                fieldWithPath("data.eupMyeonDong").type(JsonFieldType.STRING),
                fieldWithPath("data.lat").type(JsonFieldType.NUMBER),
                fieldWithPath("data.lng").type(JsonFieldType.NUMBER)
            )
    }

    @DisplayName("특정 회원의 특정 주소를 수정한다.")
    @Test
    fun addressModify() {
        val member = Member(
            email = "email1",
            password = "password",
            role = Role.USER,
        )

        given(addressService.modifyAddress(any(), any(), any()))
            .willReturn(
                Address(
                    member = member,
                    siDo = "서울특별시",
                    siGunGu = "강남구",
                    eupMyeonDong = "역삼동",
                    lat = 37.4995539438207,
                    lng = 127.031393491745,
                    fullAddress = "서울 강남구 테헤란로 123"
                )
            )

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

        responseFields(
            fieldWithPath("status").type(JsonFieldType.NUMBER),
            fieldWithPath("code").type(JsonFieldType.STRING),
            fieldWithPath("message").type(JsonFieldType.STRING),
            fieldWithPath("data").type(JsonFieldType.STRING)
        )
    }

}