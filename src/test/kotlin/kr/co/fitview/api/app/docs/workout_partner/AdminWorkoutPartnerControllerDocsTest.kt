package kr.co.fitview.api.app.docs.workout_partner

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.controller.AdminWorkoutPartnerController
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import kr.co.fitview.api.app.domain.workout_partner.controller.WorkoutPartnerController
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestQueryService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AdminWorkoutPartnerControllerDocsTest : RestDocsSupport() {

    override fun initController(): Any {
        return AdminWorkoutPartnerController()
    }

    @DisplayName("운동 파트너 신청 조회 API")
    @Test
    fun workoutPartnerRequestList() {
        // given

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/workout-partner-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("workoutPartnerRequestId", "100")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-workout-partner-request-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional, default: 10) 한 번에 조회할 데이터 수"),

                        parameterWithName("workoutPartnerRequestId").optional()
                            .description(
                                "(Optional) 커서 기준 ID. " +
                                "처음에는 null이면 최신 데이터부터 size만큼 조회, " +
                                "이후에는 가장 작은 ID를 넣으면 그 ID보다 작은 데이터(과거 데이터)를 조회"
                            ),

                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("응답 메시지"),
                        fieldWithPath("data.content[].workoutPartnerRequestId").type(JsonFieldType.NUMBER)
                            .description("생성된 운동 파트너 요청 ID"),
                        fieldWithPath("data.content[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("요청을 보낸 회원 닉네임"),
                        fieldWithPath("data.content[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("요청을 받은 회원 닉네임"),
                        fieldWithPath("data.content[].workoutPartnerRequestStatus").type(JsonFieldType.STRING)
                            .description("요청 상태"),
                        fieldWithPath("data.content[].requestedAt").type(JsonFieldType.STRING)
                            .description("요청 생성 시각"),
                        fieldWithPath("data.content[].respondedAt").type(JsonFieldType.STRING)
                            .optional()
                            .description("응답 시각"),
                        fieldWithPath("data.content[].hasChatRoom").type(JsonFieldType.BOOLEAN)
                            .description("채팅방 존재 여부"),
                        fieldWithPath("data.content[].workoutHistoryCount").type(JsonFieldType.NUMBER)
                            .description("운동 내역 수"),
                        *RestDocsPagination.paginationByCursor(),
                    )
                )
            )
    }



}