package kr.co.fitview.api.app.docs.workout_reward

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.workout_reward.controller.AdminWorkoutRewardController
import kr.co.fitview.api.app.domain.workout_reward.dto.request.AdminWorkoutRewardCouponStatusRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminWorkoutRewardControllerDocsTest : RestDocsSupport() {

    private val workoutRewardQueryService: WorkoutRewardQueryService =
        mock(WorkoutRewardQueryService::class.java)

    private val workoutRewardService: WorkoutRewardService =
        mock(WorkoutRewardService::class.java)

    override fun initController(): Any {
        return AdminWorkoutRewardController(
            workoutRewardService = workoutRewardService,
            workoutRewardQueryService = workoutRewardQueryService,
        )
    }

    @DisplayName("어드민 운동 보상 리워드 신청 내역 조회 API")
    @Test
    fun workoutRewardList() {
        // given
        val fixedTime = LocalDateTime.of(2026, 3, 8, 10, 0, 0)

        val claim1 = AdminWorkoutRewardClaimResponse(
            workoutRewardClaimId = 2L,
            nickname = "스폰지밥",
            stampLevelDisplayName = "3회",
            couponName = "[스타벅스] 5,000원",
            phoneNumber = "01011111111",
            createdAt = fixedTime,
            coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                status = WorkoutRewardClaimCouponStatus.PENDING,
                statusLabel = WorkoutRewardClaimCouponStatus.PENDING.displayName
            )
        )

        val claim2 = AdminWorkoutRewardClaimResponse(
            workoutRewardClaimId = 1L,
            nickname = "뚱이",
            stampLevelDisplayName = "5회",
            couponName = "[이마트] 10,000원",
            phoneNumber = "01022222222",
            createdAt = fixedTime.minusHours(1),
            coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                status = WorkoutRewardClaimCouponStatus.ISSUED,
                statusLabel = WorkoutRewardClaimCouponStatus.ISSUED.displayName
            )
        )

        val sliceResponse = SliceImpl(
            listOf(claim1, claim2),
            PageRequest.of(0, 10),
            false
        )

        given(workoutRewardQueryService.findAllWorkoutReward(any()))
            .willReturn(sliceResponse)


        // when & then
        mockMvc.perform(
            get("/api/v1/admins/workout-rewards")
                .header("Authorization", "Bearer admin-jwt-token")
                .param("cursorAt", "1772960400000") // 타임스탬프 예시
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-workout-reward-list-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    queryParameters(
                        parameterWithName("cursorAt").description("마지막으로 조회한 내역의 createdAt 타임스탬프 (밀리초 단위). 첫 페이지 조회 시 생략 가능.")
                            .optional(),
                        parameterWithName("size").description("한 번에 조회할 데이터 개수. 기본값 10.").optional()
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),

                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터 (커서 페이징 객체)"),

                        // Pagination 정보
                        fieldWithPath("data.pagination").type(JsonFieldType.OBJECT)
                            .description("페이징 정보"),
                        fieldWithPath("data.pagination.size").type(JsonFieldType.NUMBER)
                            .description("조회된 데이터 개수"),
                        fieldWithPath("data.pagination.cursorAt").type(JsonFieldType.NUMBER)
                            .description("마지막 원소의 시간 커서 (다음 요청 시 cursorAt 파라미터로 사용)").optional(),
                        fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN)
                            .description("다음 페이지 존재 여부"),

                        // Content 목록
                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY)
                            .description("리워드 신청 내역 목록"),
                        fieldWithPath("data.content[].workoutRewardClaimId").type(JsonFieldType.NUMBER)
                            .description("리워드 신청 ID"),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING)
                            .description("사용자 닉네임"),
                        fieldWithPath("data.content[].stampLevelDisplayName").type(JsonFieldType.STRING)
                            .description("달성 스탬프 기준 " + WorkoutRewardClaimWorkoutCount.allDescription()),
                        fieldWithPath("data.content[].couponName").type(JsonFieldType.STRING)
                            .description("신청한 쿠폰 상품명"),
                        fieldWithPath("data.content[].phoneNumber").type(JsonFieldType.STRING)
                            .description("사용자 전화번호"),
                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING)
                            .description("신청 일시 (예: 2026-03-08T10:00:00)"),

                        fieldWithPath("data.content[].coupon").type(JsonFieldType.OBJECT)
                            .description("쿠폰 처리 상태 정보"),
                        fieldWithPath("data.content[].coupon.status").type(JsonFieldType.STRING)
                            .description("상태 코드 " + WorkoutRewardClaimCouponStatus.allDescription()),
                        fieldWithPath("data.content[].coupon.statusLabel").type(JsonFieldType.STRING)
                            .description("상태 한글 라벨 " + WorkoutRewardClaimCouponStatus.allDescription())
                    )
                )
            )
    }

    @DisplayName("어드민 리워드 쿠폰 상태 변경 API")
    @Test
    fun updateWorkoutRewardCouponStatus() {
        // given
        val workoutRewardClaimId = 1L
        val request = AdminWorkoutRewardCouponStatusRequest(
            workoutRewardCouponStatus = WorkoutRewardClaimCouponStatus.ISSUED
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/admins/workout-rewards/claims/{workoutRewardClaimId}/coupon-status", workoutRewardClaimId)
                .header("Authorization", "Bearer admin-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-workout-reward-coupon-status-update",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    pathParameters(
                        parameterWithName("workoutRewardClaimId").description("상태를 변경할 리워드 신청 ID")
                    ),

                    // HTTP 요청 바디 명세
                    requestFields(
                        fieldWithPath("workoutRewardCouponStatus").type(JsonFieldType.STRING)
                            .description("변경할 쿠폰 상태 (ISSUED: 발송 완료, PENDING: 대기중)")
                    ),

                    // HTTP 응답 바디 명세
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태 코드"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 문자열")
                    )
                )
            )
    }

}