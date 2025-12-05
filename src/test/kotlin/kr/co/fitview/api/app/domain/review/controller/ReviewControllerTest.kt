package kr.co.fitview.api.app.domain.review.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ReviewControllerTest : ControllerTestSupport(){

    @DisplayName("회원이 선택 가능한 후기 서브 메시지를 전부 조회한다.")
    @Test
    fun reviewTagList() {
        // given
        val response = listOf(
            ReviewCategoryResponse(
                reviewCategoryId = 100L,
                reviewCategoryDisplayText = "운동 태도",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 101L, reviewTagDisplayText = "집중력이 좋아요"),
                    ReviewTagResponse(reviewTagId = 102L, reviewTagDisplayText = "루틴이 탄탄해요"),
                    ReviewTagResponse(reviewTagId = 103L, reviewTagDisplayText = "운동 템포가 잘 맞아요"),
                    ReviewTagResponse(reviewTagId = 104L, reviewTagDisplayText = "성실하게 임해요"),
                    ReviewTagResponse(reviewTagId = 105L, reviewTagDisplayText = "페이스가 조금 달랐어요")
                )
            ),

            ReviewCategoryResponse(
                reviewCategoryId = 200L,
                reviewCategoryDisplayText = "매너/커뮤니케이션",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 201L, reviewTagDisplayText = "운동 자세를 서로 존중해요"),
                    ReviewTagResponse(reviewTagId = 202L, reviewTagDisplayText = "시간 약속을 잘 지켜요"),
                    ReviewTagResponse(reviewTagId = 203L, reviewTagDisplayText = "피드백이 깔끔해요"),
                    ReviewTagResponse(reviewTagId = 204L, reviewTagDisplayText = "집중 흐름을 잘 맞춰줘요"),
                    ReviewTagResponse(reviewTagId = 205L, reviewTagDisplayText = "기구 사용 순서를 배려해요")
                )
            ),

            ReviewCategoryResponse(
                reviewCategoryId = 300L,
                reviewCategoryDisplayText = "에너지/분위기",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 301L, reviewTagDisplayText = "긍정적인 에너지를 주는 분이에요"),
                    ReviewTagResponse(reviewTagId = 302L, reviewTagDisplayText = "운동 분위기를 잘 이끌어요"),
                    ReviewTagResponse(reviewTagId = 303L, reviewTagDisplayText = "동기부여가 돼요"),
                    ReviewTagResponse(reviewTagId = 304L, reviewTagDisplayText = "운동 중 불필요한 방해가 없어요"),
                    ReviewTagResponse(reviewTagId = 305L, reviewTagDisplayText = "웃으면서 즐겁게 운동했어요")
                )
            ),

            ReviewCategoryResponse(
                reviewCategoryId = 400L,
                reviewCategoryDisplayText = "운동 스타일",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 401L, reviewTagDisplayText = "서로 배울 점이 많았어요"),
                    ReviewTagResponse(reviewTagId = 402L, reviewTagDisplayText = "효율적이였어요"),
                    ReviewTagResponse(reviewTagId = 403L, reviewTagDisplayText = "함께 운동하니 동기부여가 되었어요"),
                    ReviewTagResponse(reviewTagId = 404L, reviewTagDisplayText = "무게를 많이 칠 수 있어요"),
                    ReviewTagResponse(reviewTagId = 405L, reviewTagDisplayText = "반복 중심이에요")
                )
            )
        )
        given(reviewService.findReviewCategoryAndTag())
            .willReturn(
                response
            )

        // when // then
        mockMvc.perform(
            get("/api/v1/reviews/tags")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").isNotEmpty())

            .andExpect(jsonPath("$.data[0].reviewCategoryId").value(100L))
            .andExpect(jsonPath("$.data[0].reviewCategoryDisplayText").value("운동 태도"))
            .andExpect(jsonPath("$.data[0].tags.length()").value(5))

            .andExpect(jsonPath("$.data[0].tags[0].reviewTagId").value(101L))
            .andExpect(jsonPath("$.data[0].tags[0].reviewTagDisplayText").value("집중력이 좋아요"))

            .andExpect(jsonPath("$.data[3].reviewCategoryId").value(400L))
            .andExpect(jsonPath("$.data[3].reviewCategoryDisplayText").value("운동 스타일"))
            .andExpect(jsonPath("$.data[3].tags.length()").value(5))

            .andExpect(jsonPath("$.data[3].tags[4].reviewTagId").value(405L))
            .andExpect(jsonPath("$.data[3].tags[4].reviewTagDisplayText").value("반복 중심이에요"))
    }

    @DisplayName("회원 리뷰를 저장한다.")
    @Test
    fun reviewAdd() {
        // given
        val request = ReviewCreateRequest(
            workoutHistoryId = 123,
            type = ReviewType.GOOD,
            reviewTagIds = listOf(1,2,3),
            content = "content"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/reviews")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

    @DisplayName("회원 리뷰를 저장 시, 운동 이력 id는 필수다")
    @Test
    fun reviewAddRequiredWorkoutHistoryId() {
        // given
        val request = ReviewCreateRequest(
            workoutHistoryId = null,
            type = ReviewType.GOOD,
            reviewTagIds = listOf(1,2,3),
            content = "content"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/reviews")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("workoutHistoryId is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("회원 리뷰를 저장 시, 후기 타입은 필수다")
    @Test
    fun reviewAddRequiredType() {
        // given
        val request = ReviewCreateRequest(
            workoutHistoryId = 123,
            type = null,
            reviewTagIds = listOf(1,2,3),
            content = "content"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/reviews")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("type is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("회원 리뷰를 저장 시, 후기 서브 메시지 배열은 빈 배열은 안된다.")
    @Test
    fun reviewAddNotEmptyReviewTagIds() {
        // given
        val request = ReviewCreateRequest(
            workoutHistoryId = 123,
            type = ReviewType.GOOD,
            reviewTagIds = listOf(),
            content = "content"
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/reviews")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("reviewTagIds cannot be empty"))
            .andExpect(jsonPath("$.data").isEmpty())
    }
}