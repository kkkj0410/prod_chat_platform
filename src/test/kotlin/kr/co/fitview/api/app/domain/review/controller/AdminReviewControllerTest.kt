package kr.co.fitview.api.app.domain.review.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class AdminReviewControllerTest  : ControllerTestSupport(){

    @DisplayName("리뷰 이력을 전체 조회한다.")
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

        val condition = AdminReviewCondition()

        given(reviewQueryService.findAllReviewFrom(condition))
            .willReturn(slice)

        // when // then
        mockMvc.perform(
            get("/api/v1/admins/reviews")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").isNotEmpty())

            .andExpect(jsonPath("$.data.content[0].reviewId").value(99))
            .andExpect(jsonPath("$.data.content[0].workoutPartnerId").value(10))
            .andExpect(jsonPath("$.data.content[0].workoutHistoryId").value(990))
            .andExpect(jsonPath("$.data.content[0].fromMemberNickname").value("FromUser99"))
            .andExpect(jsonPath("$.data.content[0].toMemberNickname").value("ToUser99"))
            .andExpect(jsonPath("$.data.content[0].reviewType").value("NORMAL"))

            .andExpect(jsonPath("$.data.content[0].reviewTagDisplayTexts").isArray())
            .andExpect(jsonPath("$.data.content[0].reviewTagDisplayTexts.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].reviewTagDisplayTexts[0]").value("친절해요"))
            .andExpect(jsonPath("$.data.content[0].reviewTagDisplayTexts[1]").value("운동 설명이 좋아요"))

            .andExpect(jsonPath("$.data.content[0].reviewContent").isEmpty())
            .andExpect(jsonPath("$.data.content[0].postedAt").exists())
    }


}