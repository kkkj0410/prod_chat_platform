package kr.co.fitview.api.app.docs.review

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
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset


class ReviewControllerDocsTest : RestDocsSupport() {

    private val reviewService: ReviewService = mock(ReviewService::class.java)

    override fun initController(): Any {
        return ReviewController(reviewService)
    }

    @DisplayName("후기 서브 메시지 조회")
    @Test
    fun reviewTagList() {

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

            .andDo(
                document(
                    "review-tag-get",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),


                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),

                        fieldWithPath("data").type(JsonFieldType.ARRAY)
                            .description("후기 카테고리 및 태그 목록"),

                        fieldWithPath("data[].reviewCategoryId").type(JsonFieldType.NUMBER)
                            .description("카테고리 ID (PK, Enum 값)"),
                        fieldWithPath("data[].reviewCategoryDisplayText").type(JsonFieldType.STRING)
                            .description("카테고리 명칭 (UI 표시 텍스트)"),


                        fieldWithPath("data[].tags").type(JsonFieldType.ARRAY)
                            .description("해당 카테고리에 속한 태그 목록"),
                        fieldWithPath("data[].tags[].reviewTagId").type(JsonFieldType.NUMBER)
                            .description("태그 ID (PK, Enum 값)"),
                        fieldWithPath("data[].tags[].reviewTagDisplayText").type(JsonFieldType.STRING)
                            .description("태그 명칭 (UI 표시 텍스트)")
                    )
                )
            )
    }



}