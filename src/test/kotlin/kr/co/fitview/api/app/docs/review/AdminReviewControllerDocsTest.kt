package kr.co.fitview.api.app.docs.review

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.controller.AdminReviewController
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewService
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
import org.springframework.data.domain.PageRequest
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
import java.time.LocalDateTime


class AdminReviewControllerDocsTest : RestDocsSupport() {

    private val reviewQueryService: ReviewQueryService = mock(ReviewQueryService::class.java)

    override fun initController(): Any {
        return AdminReviewController(reviewQueryService)
    }

    @DisplayName("리뷰 조회 API")
    @Test
    fun reviewList() {
        // given
        val response = listOf(
            AdminReviewResponse(
                fromMemberId = 1L,
                toMemberId = 101L,
                reviewId = 99L,
                workoutPartnerId = 10L,
                workoutHistoryId = 990L,
                fromMemberNickname = "FromUser99",
                toMemberNickname = "ToUser99",
                reviewType = ReviewType.NORMAL,
                reviewTagDisplayTexts = listOf("친절해요", "운동 설명이 좋아요"),
                reviewContent = null,
                postedAt = LocalDateTime.now()
            )
        )

        val pageable = PageRequest.of(0, 10)

        val slice: Slice<AdminReviewResponse> =
            SliceImpl(response, pageable, false)

        given(reviewQueryService.findAllReviewFrom(any()))
            .willReturn(slice)

        // when & then
        mockMvc.perform(
            get("/api/v1/admins/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("reviewId", "100")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "admin-review-list-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.ADMIN)
                    ),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional, default: 10) 한 번에 조회할 데이터 수"),

                        parameterWithName("reviewId").optional()
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
                        fieldWithPath("data.content[].reviewId").type(JsonFieldType.NUMBER)
                            .description("리뷰 ID"),

                        fieldWithPath("data.content[].workoutPartnerId").type(JsonFieldType.NUMBER)
                            .description("운동 파트너 ID"),
                        fieldWithPath("data.content[].workoutHistoryId").type(JsonFieldType.NUMBER)
                            .description("운동 이력 ID"),
                        fieldWithPath("data.content[].fromMemberNickname").type(JsonFieldType.STRING)
                            .description("리뷰 작성자 닉네임"),
                        fieldWithPath("data.content[].toMemberNickname").type(JsonFieldType.STRING)
                            .description("리뷰 대상자 닉네임"),
                        fieldWithPath("data.content[].reviewType").type(JsonFieldType.STRING)
                            .description("리뷰 타입"),
                        fieldWithPath("data.content[].reviewTagDisplayTexts").type(JsonFieldType.ARRAY)
                            .description("리뷰 태그 목록"),
                        fieldWithPath("data.content[].reviewContent").type(JsonFieldType.STRING)
                            .optional()
                            .description("리뷰 내용"),
                        fieldWithPath("data.content[].postedAt").type(JsonFieldType.STRING)
                            .description("리뷰 작성 시각"),

                        *RestDocsPagination.paginationByCursor(),
                    )
                )
            )
    }



}