package kr.co.fitview.api.app.docs.member

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.entity.Role
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
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status




class MemberControllerDocsTest : RestDocsSupport() {

    private val memberService: MemberService = mock(MemberService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return MemberController(memberService, securityUtil)
    }

    @DisplayName("사용자 본인 조회 API")
    @Test
    fun memberMe() {
        // when
        given(memberService.findMemberMe(any()))
            .willReturn(
                MemberMeResponse(
                    email = "email",
                    role = Role.USER
                )
            )

        // then
        mockMvc.perform(
            get("/api/v1/members/me")
            .header("Authorization", "Bearer jwt-token")

        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(document("member-me",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),

            requestHeaders(
                RestDocsHeaders.authorizationHeader(Role.USER)
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
                fieldWithPath("data.email").type(JsonFieldType.STRING)
                    .description("로그인 id"),
                fieldWithPath("data.role").type(JsonFieldType.STRING)
                    .description("회원 역할")
            )
            ))
    }

    @DisplayName("본인 주소 조회 API")
    @Test
    fun memberAddressDetails() {

        // when & then
        mockMvc.perform(
            get("/api/v1/members/{memberId}/addresses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-address-details",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                pathParameters(
                    parameterWithName("memberId").description("조회 대상 회원 id")
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
                    fieldWithPath("data.address").type(JsonFieldType.STRING)
                        .description("회원 전체 주소. 도로명 주소 or 사용자가 입력한 주소값이 해당 장소에 옴. 서울 외 지역 사람은 서울시 서초구 서초동으로 반환"),
                )
            ))
    }


    @DisplayName("추천 핏버디 API")
    @Test
    fun memberRecommendationList() {
        // when & then
        mockMvc.perform(
            get("/api/v1/members/recommendations")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-recommendation",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
                ),

                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER)
                        .description("상태"),
                    fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("에러 메시지"),
                    fieldWithPath("data").type(JsonFieldType.ARRAY)
                        .description("응답 데이터 - 배열 내부에 10개 원소 제한으로 응답"),
                    fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER)
                        .description("조회 대상 회원 id"),
                    fieldWithPath("data[].nickname").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 별명"),
                    fieldWithPath("data[].workoutExperience").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 경력"),
                    fieldWithPath("data[].workoutStyle").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 스타일"),
                    fieldWithPath("data[].workoutGoal").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 목표"),
                )
            ))
    }


    @DisplayName("우리 동네 핏버디 API")
    @Test
    fun memberLocalList() {
        // when & then
        mockMvc.perform(
            get("/api/v1/members/local")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("member-local",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),

                requestHeaders(
                    RestDocsHeaders.authorizationHeader(Role.USER)
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
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY)
                        .description("응답 데이터 목록"),
                    fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER)
                        .description("조회 대상 회원 id"),
                    fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 별명"),
                    fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 경력"),
                    fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 스타일"),
                    fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING)
                        .description("조회 대상 회원 운동 목표"),

                    *RestDocsPagination.paginationFields()
                )
            ))
    }


}