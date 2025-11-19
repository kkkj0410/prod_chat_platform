package kr.co.fitview.api.app.docs.workout_partner

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.workout_partner.controller.WorkoutPartnerController
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
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
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class WorkoutPartnerControllerDocsTest : RestDocsSupport() {

    private val workoutPartnerRequestService: WorkoutPartnerRequestService = mock(WorkoutPartnerRequestService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return WorkoutPartnerController(workoutPartnerRequestService, securityUtil)
    }

    @DisplayName("핏버디 요청 API")
    @Test
    fun workoutPartnerAdd() {
        // given
        val request = WorkoutPartnerCreateRequest(
            memberId = 123L
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/workout-partners")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-partner-add",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("memberId").type(JsonFieldType.NUMBER)
                            .description("핏버디 요청할 상대 회원 id"),
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


    @DisplayName("핏버디 요청 응답 API")
    @Test
    fun workoutPartnerModify() {
        // given
        val request = WorkoutPartnerUpdateRequest(
            type = WorkoutPartnerRequestUpdateStatus.ACCEPT
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/workout-partners/{workoutPartnerRequestId}", 123)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-partner-request-update",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("workoutPartnerRequestId").description("해당 운동 파트너 요청 id.")
                    ),

                    requestFields(
                        fieldWithPath("type").type(JsonFieldType.STRING)
                            .description("응답 유형" + WorkoutPartnerRequestUpdateStatus.allDescription()),
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

    @DisplayName("운동 파트너 요청 리스트 API")
    @Test
    fun workoutPartnerRequestList() {

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-partner-requests")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastWorkoutPartnerRequestId", "123")
                .param("type", "SEND")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-partner-request-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastWorkoutPartnerRequestId").optional()
                            .description("(Optional) 최근까지 조회한 id 값 - lastWorkoutPartnerRequestId(처음에는 null로 했을 때 최근 것을 size 개수만큼 가져옴. 그 후, lastWorkoutRequestId 호출하면 그 이후 데이터 가져옴)"),
                        parameterWithName("type")
                            .description("운동 파트너 요청 타입" + WorkoutPartnerRequestType.allDescription()),
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
                        fieldWithPath("data.content[].workoutPartnerRequestId").type(JsonFieldType.NUMBER)
                            .description("운동 파트너 요청 id"),
                        fieldWithPath("data.content[].profileImageUrl").type(JsonFieldType.STRING)
                            .description("회원 프로필 이미지 URL"),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                            .description("회원 닉네임"),
                        fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING)
                            .description("회원 운동 경력"),
                        fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING)
                            .description("회원 운동 스타일"),
                        fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING)
                            .description("회원 운동 목표"),
                        fieldWithPath("data.content[].status").type(JsonFieldType.STRING)
                            .description("운동 파트너 요청 상태" + WorkoutPartnerRequestStatusForResponse.allDescription()),
                        fieldWithPath("data.content[].chatRoomId").type(JsonFieldType.NUMBER)
                            .optional()
                            .description("파트너 관계일 경우 채팅방 id, 없으면 null"),


                        *RestDocsPagination.paginationByCursor()
                    )
                )
            )
    }

}