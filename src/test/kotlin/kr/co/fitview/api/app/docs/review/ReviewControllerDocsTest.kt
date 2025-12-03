package kr.co.fitview.api.app.docs.review

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.chat.controller.ChatController
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomService
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.chat.service.MessageReadStatusService
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfile
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.review.controller.ReviewController
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
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