package kr.co.fitview.api.app.docs.workout

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
import kr.co.fitview.api.app.domain.workout.controller.AdminWorkoutController
import kr.co.fitview.api.app.domain.workout.dto.response.*
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_history.controller.WorkoutHistoryController
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


class AdminWorkoutControllerDocsTest : RestDocsSupport() {

    private val workoutRequestQueryService: WorkoutRequestQueryService = mock(WorkoutRequestQueryService::class.java)

    override fun initController(): Any {
        return AdminWorkoutController(workoutRequestQueryService)
    }

    @DisplayName("운동 이력 조회 API")
    @Test
    fun workoutRequestList() {
        // given
        val now = LocalDateTime.now()

        val request1 = AdminWorkoutRequestResponse(
            fromMemberId = 1L,
            toMemberId = 1L,
            workoutRequestId = 101L,
            fromMemberNickname = "Alice",
            toMemberNickname = "Bob",
            workoutRequestStatus = WorkoutRequestStatus.PENDING,
            requestedAt = now.minusDays(2),
            scheduledAt = now.plusDays(1),
            location = "Gym A",
            hasFromMemberReview = true,
            hasToMemberReview = false
        )

        val request2 = AdminWorkoutRequestResponse(
            fromMemberId = 2L,
            toMemberId = 2L,
            workoutRequestId = 102L,
            fromMemberNickname = "Charlie",
            toMemberNickname = "David",
            workoutRequestStatus = WorkoutRequestStatus.ACCEPT,
            requestedAt = now.minusDays(1),
            scheduledAt = now.plusDays(2),
            location = "Gym B",
            hasFromMemberReview = false,
            hasToMemberReview = false
        )

        val content = listOf(request1, request2)

        val pageRequest = PageRequest.of(0, 10)
        val slice = SliceImpl(content, pageRequest, false)

        given(workoutRequestQueryService.findAllWorkoutRequestFrom(any()))
            .willReturn(slice)

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/workout-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("workoutRequestId", "100")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-request-list-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional, default: 10) 한 번에 조회할 데이터 수"),

                        parameterWithName("workoutRequestId").optional()
                            .description(
                                "(Optional) 커서 기준 ID. " +
                                        "처음에는 null이면 최신 데이터부터 size만큼 조회, " +
                                        "이후에는 가장 작은 ID를 넣으면 그 ID보다 작은 데이터(과거 데이터)를 조회"
                            ),

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
                        fieldWithPath("data.content[].workoutPartnerId").type(JsonFieldType.NUMBER)
                            .description("운동 파트너 ID"),
                        fieldWithPath("data.content[].workoutRequestId").type(JsonFieldType.NUMBER)
                            .description("생성된 운동 요청 ID"),
                        fieldWithPath("data.content[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("요청을 보낸 회원 닉네임"),
                        fieldWithPath("data.content[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("요청을 받은 회원 닉네임"),
                        fieldWithPath("data.content[].workoutRequestStatus").type(JsonFieldType.STRING)
                            .description("요청 상태 (PENDING, ACCEPT, REJECT, CANCEL, COMPLETE, EXPIRE)"),
                        fieldWithPath("data.content[].requestedAt").type(JsonFieldType.STRING)
                            .description("요청 생성 시각"),
                        fieldWithPath("data.content[].respondedAt").type(JsonFieldType.STRING)
                            .optional()
                            .description("응답 시각 (없을 수 있음)"),
                        fieldWithPath("data.content[].scheduledAt").type(JsonFieldType.STRING)
                            .description("운동 예정 시각"),
                        fieldWithPath("data.content[].location").type(JsonFieldType.STRING)
                            .description("운동 장소"),
                        fieldWithPath("data.content[].hasFromMemberReview").type(JsonFieldType.BOOLEAN)
                            .description("요청 보낸 회원의 리뷰 작성 여부"),
                        fieldWithPath("data.content[].hasToMemberReview").type(JsonFieldType.BOOLEAN)
                            .description("요청 받은 회원의 리뷰 작성 여부"),
                        *RestDocsPagination.paginationByCursor(),
                    )
                )
            )
    }

    @DisplayName("운동 요청 상세 조회 API")
    @Test
    fun workoutRequestDetail() {
        // given
        val workoutRequestId = 1L

        val workoutLogs = listOf(
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.PENDING,
                loggedAt = LocalDateTime.now().minusDays(3),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            ),
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.ACCEPT,
                loggedAt = LocalDateTime.now().minusDays(2),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            ),
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.COMPLETE,
                loggedAt = LocalDateTime.now().minusDays(1),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            )
        )

        val reviews = (1..2).map { i ->
            AdminDetailReviewResponse(
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser",
                postedAt = LocalDateTime.now().minusDays(2 - i.toLong())
            )
        }.sortedBy { it.postedAt }

        val response = AdminDetailWorkoutRequestResponse(
            workoutPartnerId = 1L,
            workoutRequestId = workoutRequestId,
            scheduledAt = LocalDateTime.now().plusDays(3),
            location = "서울 강남구 헬스장 101",
            workoutRequestLogs = workoutLogs,
            reviews = reviews
        )

        given(workoutRequestQueryService.findWorkoutRequestDetail(any()))
            .willReturn(response)

        mockMvc.perform(
            get("/api/v1/admins/workout-requests/{workoutRequestId}", workoutRequestId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-request-detail-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    pathParameters(
                        parameterWithName("workoutRequestId").description("조회할 운동 요청 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("응답 메시지"),
                        fieldWithPath("data.workoutPartnerId").type(JsonFieldType.NUMBER)
                            .description("운동 파트너 ID"),
                        fieldWithPath("data.workoutRequestId").type(JsonFieldType.NUMBER)
                            .description("운동 요청 ID"),
                        fieldWithPath("data.scheduledAt").type(JsonFieldType.STRING)
                            .description("운동 예정 시각"),
                        fieldWithPath("data.location").type(JsonFieldType.STRING)
                            .description("운동 장소"),

                        fieldWithPath("data.workoutRequestLogs[]").type(JsonFieldType.ARRAY)
                            .description("운동 요청 상태 로그 리스트"),
                        fieldWithPath("data.workoutRequestLogs[].workoutRequestStatus").type(JsonFieldType.STRING)
                            .description("로그 상태" + WorkoutRequestStatus.allDescription()),
                        fieldWithPath("data.workoutRequestLogs[].loggedAt").type(JsonFieldType.STRING)
                            .description("로그 생성 시각"),
                        fieldWithPath("data.workoutRequestLogs[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("로그 작성 회원 닉네임 (보낸 사람)"),
                        fieldWithPath("data.workoutRequestLogs[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("로그 작성 회원 닉네임 (받는 사람)"),

                        fieldWithPath("data.reviews[]").type(JsonFieldType.ARRAY)
                            .description("리뷰 리스트 (오래된 순)"),
                        fieldWithPath("data.reviews[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("리뷰 작성자 닉네임"),
                        fieldWithPath("data.reviews[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("리뷰 대상자 닉네임"),
                        fieldWithPath("data.reviews[].postedAt").type(JsonFieldType.STRING)
                            .description("리뷰 작성 시각")
                    )
                )
            )
    }

}