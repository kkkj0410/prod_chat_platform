package kr.co.fitview.api.app.docs.workout_reward

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.workout_reward.controller.WorkoutRewardController
import kr.co.fitview.api.app.domain.workout_reward.dto.request.WorkoutRewardClaimRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardPolicyResponse
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardStampMeResponse
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WorkoutRewardControllerDocsTest : RestDocsSupport() {

    private val workoutRewardQueryService: WorkoutRewardQueryService =
        mock(WorkoutRewardQueryService::class.java)

    private val securityUtil: SecurityUtil =
        mock(SecurityUtil::class.java)

    private val workoutRewardService: WorkoutRewardService =
        mock(WorkoutRewardService::class.java)

    override fun initController(): Any {
        return WorkoutRewardController(
            workoutRewardService = workoutRewardService,
            workoutRewardQueryService = workoutRewardQueryService,
            securityUtil = securityUtil
        )
    }

    @DisplayName("운동 보상 리워드 정책 조회 API")
    @Test
    fun workoutRewardPolicyDetails() {
        // given
        val response = WorkoutRewardPolicyResponse(
            isActive = true,
            stamp = WorkoutRewardPolicyResponse.Stamp(
                first = WorkoutRewardPolicyResponse.StampDetail(
                    price = 5000,
                    priceDisplayName = "5천원"
                ),
                second = WorkoutRewardPolicyResponse.StampDetail(
                    price = 10000,
                    priceDisplayName = "1만원"
                )
            ),
            coupons = listOf(
                WorkoutRewardPolicyResponse.Coupon(
                    type = WorkoutRewardPolicyResponse.CouponType.BAEMIN,
                    iconPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/icon/5b492e56-4bad-4aab-ae39-b21f47d8c995",
                    firstCardPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/cf44c0f6-3faa-411d-9016-1abb28914f1e",
                    secondCardPngImageUrl = "https://static-dev.fitview.co.kr/workout-reward/card/17e2d851-5ea4-4fbe-a7a4-7ae098e03563"
                )
            ),
            policyNotices = listOf(
                "같은 날 여러번 운동해도 스탬프는 1개만 적립돼요.",
                "후기를 작성해야 스탬프가 적립돼요"
            )
        )

        given(workoutRewardQueryService.findWorkoutRewardPolicy())
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/policy")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-reward-policy-get",
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

                        fieldWithPath("data.isActive").type(JsonFieldType.BOOLEAN)
                            .description("운동 보상 리워드 이벤트 활성화 여부 (false면 이벤트 미노출)"),

                        fieldWithPath("data.stamp").type(JsonFieldType.OBJECT)
                            .description("스탬프 보상 정보 객체"),
                        fieldWithPath("data.stamp.first").type(JsonFieldType.OBJECT)
                            .description("첫 번째 스탬프 달성 보상"),
                        fieldWithPath("data.stamp.first.price").type(JsonFieldType.NUMBER)
                            .description("첫 번째 보상 금액"),
                        fieldWithPath("data.stamp.first.priceDisplayName").type(JsonFieldType.STRING)
                            .description("첫 번째 보상 금액 노출 텍스트"),
                        fieldWithPath("data.stamp.second").type(JsonFieldType.OBJECT)
                            .description("두 번째 스탬프 달성 보상"),
                        fieldWithPath("data.stamp.second.price").type(JsonFieldType.NUMBER)
                            .description("두 번째 보상 금액"),
                        fieldWithPath("data.stamp.second.priceDisplayName").type(JsonFieldType.STRING)
                            .description("두 번째 보상 금액 노출 텍스트"),

                        fieldWithPath("data.coupons").type(JsonFieldType.ARRAY)
                            .description("리워드 쿠폰 브랜드 목록"),
                        fieldWithPath("data.coupons[].type").type(JsonFieldType.STRING)
                            .description("쿠폰 브랜드 타입 (EMART, BAEMIN, GS25, STARBUCKS, COUPANG, NAVER_PAY)"),
                        fieldWithPath("data.coupons[].iconPngImageUrl").type(JsonFieldType.STRING)
                            .description("브랜드 아이콘 이미지 URL"),
                        fieldWithPath("data.coupons[].firstCardPngImageUrl").type(JsonFieldType.STRING)
                            .description("첫 번째 달성 카드 이미지 URL"),
                        fieldWithPath("data.coupons[].secondCardPngImageUrl").type(JsonFieldType.STRING)
                            .description("두 번째 달성 카드 이미지 URL"),

                        fieldWithPath("data.policyNotices").type(JsonFieldType.ARRAY)
                            .description("정책 유의사항 문구 목록 (String 배열)")
                    )
                )
            )
    }

    @DisplayName("내 운동 보상 리워드 스탬프 조회 API")
    @Test
    fun workoutRewardStampDetails() {
        // given
        val response = WorkoutRewardStampMeResponse(
            stampCount = 3
        )

        given(workoutRewardQueryService.findWorkoutRewardStamp(any(), any()))
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/stamps/me")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-reward-stamp-me-get",
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

                        fieldWithPath("data.stampCount").type(JsonFieldType.NUMBER)
                            .description("현재 적립된 스탬프 개수 (0부터 5 사이의 값)")
                    )
                )
            )
    }

    @DisplayName("활성화된 리워드 쿠폰 상태 조회 API")
    @Test
    fun workoutRewardCouponActive() {
        // given
        // 1. SecurityUtil 모킹 (필수: 컨트롤러 내부에서 사용 중)
        given(securityUtil.getMemberId()).willReturn(1L)

        // 2. 가짜 응답 데이터 생성 (예시로 하나는 받을 수 있고, 하나는 아직 못 받는 상태)
        val response = WorkoutRewardCouponStatusResponse(
            firstCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE,
            secondCouponStatus = WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
        )

        // 3. 서비스 로직 모킹
        given(workoutRewardQueryService.findWorkoutRewardCouponStatus(any()))
            .willReturn(response)

        // when & then
        mockMvc.perform(
            get("/api/v1/workout-rewards/coupons/active")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-reward-coupon-active-get",
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

                        fieldWithPath("data.firstCouponStatus").type(JsonFieldType.STRING)
                            .description("첫 번째 쿠폰 상태 (CLAIMABLE: 받을 수 있음, UNAVAILABLE: 받을 수 없음, CLAIMED: 이미 받음)"),
                        fieldWithPath("data.secondCouponStatus").type(JsonFieldType.STRING)
                            .description("두 번째 쿠폰 상태 (CLAIMABLE: 받을 수 있음, UNAVAILABLE: 받을 수 없음, CLAIMED: 이미 받음)")
                    )
                )
            )
    }

    @DisplayName("운동 리워드 쿠폰 발급 요청 API")
    @Test
    fun workoutRewardClaim() {
        // given
        // 1. SecurityUtil 모킹
        given(securityUtil.getMemberId()).willReturn(1L)

        // 2. 요청(Request) 데이터 생성
        val request = WorkoutRewardClaimRequest(
            phoneNumber = "01012345678",
            workoutRewardCouponType = WorkoutRewardClaimRequest.CouponType.BAEMIN,
            workoutRewardCouponLevel = WorkoutRewardClaimRequest.CouponLevel.FIRST
        )

        // 참고: workoutRewardService.addWorkoutRewardClaim()은 반환형이 없는(Unit/void) 함수이므로
        // 별도의 given(willReturn) 처리를 하지 않아도 Mockito가 기본적으로 무시(pass)하고 넘어갑니다.

        // when & then
        mockMvc.perform(
            post("/api/v1/workout-rewards/claims")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)) // JSON 직렬화
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "workout-reward-claim-post",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                            .description("전화번호 (예: 01012345678)"),
                        fieldWithPath("workoutRewardCouponType").type(JsonFieldType.STRING)
                            .description("요청할 쿠폰 브랜드 타입 (BAEMIN, NAVER_PAY, COUPANG, GS25, EMART, STARBUCKS)"),
                        fieldWithPath("workoutRewardCouponLevel").type(JsonFieldType.STRING)
                            .description("요청할 쿠폰 레벨 (FIRST, SECOND)")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("응답 데이터")
                    )
                )
            )
    }
}