package kr.co.fitview.api.app.docs.workout_history

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.controller.ChatController
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomService
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.chat.service.MessageReadStatusService
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfile
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.member.dto.response.MemberProfileResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.review.controller.ReviewController
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_history.controller.WorkoutHistoryController
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryRecentResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.enums.WorkoutHistoryReviewStatus
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.enums.Direction
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.*
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset


class WorkoutHistoryControllerDocsTest : RestDocsSupport() {

    private val workoutHistoryQueryService: WorkoutHistoryQueryService = mock(WorkoutHistoryQueryService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return WorkoutHistoryController(workoutHistoryQueryService, securityUtil)
    }

    @DisplayName("리뷰 상태 조회 API")
    @Test
    fun workoutHistoryReviewStatus() {
        // given
        given(workoutHistoryQueryService.findWorkoutHistoryReviewStatus(any(), any()))
            .willReturn(
                WorkoutHistoryReviewStatusResponse(
                    status = WorkoutHistoryReviewStatus.WRITABLE
                )
            )

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-histories/{workoutHistoryId}/reviews/statuses", 1)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-history-review-status",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("workoutHistoryId")
                            .description("운동 기록 ID")
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
                        fieldWithPath("data.status").type(JsonFieldType.STRING)
                            .description("리뷰 상태" + WorkoutHistoryReviewStatus.allDescription()),

                    )
                )
            )
    }


    @DisplayName("최근 운동 기록 리스트 조회 API")
    @Test
    fun workoutHistoryRecentList() {
        // given
        // 1. 컨트롤러 내부에서 호출되는 securityUtil 모킹
        given(securityUtil.getMemberId()).willReturn(1L)

        // 2. 응답으로 내려줄 가짜 리스트 생성
        val responseList = listOf(
            WorkoutHistoryRecentResponse(
                workoutHistoryId = 2L,
                nickname = "스폰지밥",
                completedAt = LocalDateTime.of(2024, 1, 3, 10, 0, 0),
                isReviewed = true
            ),
            WorkoutHistoryRecentResponse(
                workoutHistoryId = 1L,
                nickname = "뚱이",
                completedAt = LocalDateTime.of(2024, 1, 2, 9, 30, 0),
                isReviewed = false
            )
        )

        // 3. 서비스 호출 모킹 (any()를 사용하여 어떤 값이 들어가도 responseList 반환)
        given(workoutHistoryQueryService.findWorkoutHistoryRecentList(any()))
            .willReturn(responseList)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-histories/recent")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-history-recent-list-get",
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
                            .description("응답 데이터 (최근 운동 기록 리스트) - 기록이 없으면 빈 배열([]) 반환"),

                        // 배열 내부 객체 필드 명세
                        fieldWithPath("data[].workoutHistoryId").type(JsonFieldType.NUMBER)
                            .description("운동 기록 ID"),
                        fieldWithPath("data[].nickname").type(JsonFieldType.STRING)
                            .description("상대방 닉네임"),
                        fieldWithPath("data[].completedAt").type(JsonFieldType.STRING)
                            .description("운동 완료 일시"),
                        fieldWithPath("data[].isReviewed").type(JsonFieldType.BOOLEAN)
                            .description("해당 운동 기록에 대한 후기 작성 여부 (true면 작성 완료)")
                    )
                )
            )
    }

}