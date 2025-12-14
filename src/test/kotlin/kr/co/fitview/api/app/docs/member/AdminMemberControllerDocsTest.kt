package kr.co.fitview.api.app.docs.member

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.controller.AdminMemberController
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime


class AdminMemberControllerDocsTest : RestDocsSupport() {


    override fun initController(): Any {
        return AdminMemberController()
    }

    @DisplayName("사용자 조회 API")
    @Test
    fun memberList() {
        // given

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/members")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("memberId", "100")

        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-member-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10)한 페이지에 조회할 회원 수. 기본 memberId가 작은 순 -> 큰 순으로 조회됨"),
                        parameterWithName("memberId").description("(Optional - default 10)마지막으로 조회된 회원 ID. 해당 ID 이후 데이터 조회.")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("회원 리스트"),
                        fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
                        fieldWithPath("data.content[].email").type(JsonFieldType.STRING).description("회원 이메일"),
                        fieldWithPath("data.content[].provider").type(JsonFieldType.STRING).description("OAuth2 제공자"),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING).description("닉네임"),
                        fieldWithPath("data.content[].gender").type(JsonFieldType.STRING).description("성별"),
                        fieldWithPath("data.content[].birthday").type(JsonFieldType.STRING).description("생년월일"),
                        fieldWithPath("data.content[].height").type(JsonFieldType.NUMBER).description("키"),
                        fieldWithPath("data.content[].weight").type(JsonFieldType.NUMBER).description("몸무게"),
                        fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING).description("운동 경력"),
                        fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING).description("운동 스타일"),
                        fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING).description("운동 목표"),
                        fieldWithPath("data.content[].hasWorkoutImageUrl").type(JsonFieldType.BOOLEAN).description("운동 이미지 존재 여부"),
                        fieldWithPath("data.content[].fullAddress").type(JsonFieldType.STRING).description("전체 주소"),
                        fieldWithPath("data.content[].reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
                        *RestDocsPagination.paginationByCursor()

                    )
                )
            )
    }


}