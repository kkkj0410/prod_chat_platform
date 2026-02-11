package kr.co.fitview.api.app.docs.favorite

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.chat.controller.ChatController
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.service.*
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.favorite.controller.FavoriteController
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.service.FavoriteQueryService
import kr.co.fitview.api.app.domain.favorite.service.FavoriteService
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfile
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.LastWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.enums.Direction
import kr.co.fitview.api.app.global.slice.SliceWithBefore
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willDoNothing
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
import java.util.*


class FavoriteControllerDocsTest : RestDocsSupport() {

    private val favoriteService: FavoriteService = mock(FavoriteService::class.java)
    private val favoriteQueryService: FavoriteQueryService = mock(FavoriteQueryService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return FavoriteController(favoriteService, favoriteQueryService, securityUtil)
    }

    @DisplayName("찜 회원 조회 API")
    @Test
    fun favoriteMember() {

        val profileImageUrls = listOf(
            "https://static-dev.fitview.co.kr/member/profile/05fb391f-217b-433a-a160-1269f99a1f17",
            "https://static-dev.fitview.co.kr/member/profile/0419cb6d-3df1-4c15-bc3b-b95de5d0d739",
            "https://static-dev.fitview.co.kr/member/profile/5c7cff75-46e1-47e3-a738-2c2c1279559d",
            "https://static-dev.fitview.co.kr/member/profile/a5c96d4f-fc60-4909-a3c3-99db79fc6df2",
            "https://static-dev.fitview.co.kr/member/profile/3ada59e8-ac00-4237-8498-f7ce3405d7da",
            "https://static-dev.fitview.co.kr/member/profile/feb68e25-4f13-4bf6-8fe0-0334b66c37a5",
            "https://static-dev.fitview.co.kr/member/profile/419e3b60-9d60-4b34-8820-bcd7ade1510f"
        )

        val workoutImageUrls = listOf(
            "https://static-dev.fitview.co.kr/member/workout/9e489fbe-99f4-41b3-8739-ae78c7d06321",
            "https://static-dev.fitview.co.kr/member/workout/86835b04-d97a-4d57-9d4b-15f04a9c6549",
            "https://static-dev.fitview.co.kr/member/workout/4271bcdd-fd7d-4100-9c12-96c4f8abc069",
            "https://static-dev.fitview.co.kr/member/workout/1f1b1c0c-3497-48d7-8aa5-959a16409293",
            "https://static-dev.fitview.co.kr/member/workout/b5ca6537-3c9b-45b4-97a3-dd8a66fd71ec",
            "https://static-dev.fitview.co.kr/member/workout/f5deced7-b54b-488e-8c6e-3cb59656b21d"
        )

        // 1. 100개의 가짜 데이터 생성 (1 ~ 100)
        val random = Random()
        val allData = (1..10).map { i ->
            val id = i.toLong()

            val randomMemberId = (1000L..99999L).random()

            val profileImg = profileImageUrls[(i - 1) % profileImageUrls.size]
            val workoutImg = workoutImageUrls[(i - 1) % workoutImageUrls.size]

            val requestResponse = if (random.nextBoolean()) {
                val status =
                    if (random.nextBoolean()) WorkoutPartnerRequestStatus.PENDING else WorkoutPartnerRequestStatus.ACCEPT
                val chatRoomId = if (status == WorkoutPartnerRequestStatus.ACCEPT && random.nextBoolean()) {
                    id + 1000
                } else {
                    null
                }
                LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = id * 10,
                    status = status,
                    chatRoomId = chatRoomId
                )
            } else {
                null
            }

            FavoriteMemberResponse(
                favoriteId = id,
                memberId = randomMemberId,
                nickname = "헬스장고인물_$randomMemberId",
                workoutExperience = MemberWorkoutExperience.entries.toTypedArray().random(),
                workoutStyle = MemberWorkoutStyle.entries.toTypedArray().random(),
                workoutGoal = MemberWorkoutGoal.entries.toTypedArray().random(),
                profileImageUrl = profileImg,
                workoutImageUrl = if (random.nextBoolean()) workoutImg else null,
                lastWorkoutPartnerRequest = requestResponse
            )
        }.sortedByDescending { it.favoriteId }


        val pageable = PageRequest.of(0, 10)

        val slice = SliceImpl(allData, pageable, true)

        given(favoriteQueryService.findFavoriteMembers(
            any(),
            any()
        )).willReturn(slice)


        mockMvc.perform(
            get("/api/v1/favorites/members")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10") // 페이지 사이즈
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "favorite-members-list", // 문서 ID
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        // 기존에 사용하시던 헤더 유틸 사용
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").description("조회할 개수 (Default: 10)").optional(),
                        parameterWithName("cursorFavoriteId").description("커서 ID (직전에 조회한 마지막 아이템의 favoriteId). 첫 조회 시 null").optional()

                    ),

                    responseFields(
                        // 1. 공통 응답 필드
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터"),

                        // 3. 실제 데이터 리스트 (content)
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("찜한 회원 리스트"),
                        fieldWithPath("data.content[].favoriteId").type(JsonFieldType.NUMBER).description("찜 ID (커서용)"),
                        fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER).description("회원 ID"),
                        fieldWithPath("data.content[].nickname").type(JsonFieldType.STRING).description("회원 닉네임"),

                        // Enum 타입들
                        fieldWithPath("data.content[].workoutExperience").type(JsonFieldType.STRING).description("운동 경력 (BEGINNER, INTERMEDIATE, ADVANCED, PRO)"),
                        fieldWithPath("data.content[].workoutStyle").type(JsonFieldType.STRING).description("운동 스타일"),
                        fieldWithPath("data.content[].workoutGoal").type(JsonFieldType.STRING).description("운동 목표"),

                        // 이미지 URL
                        fieldWithPath("data.content[].profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
                        fieldWithPath("data.content[].workoutImageUrl").type(JsonFieldType.STRING).description("오운완 이미지 URL (없을 수 있음)").optional(),

                        // 4. 마지막 파트너 요청 정보 (Optional)
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest").type(JsonFieldType.OBJECT).description("마지막 운동 파트너 요청 정보 (없으면 null)").optional(),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.workoutPartnerRequestId").type(JsonFieldType.NUMBER).description("요청 ID"),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.status").type(JsonFieldType.STRING).description("요청 상태 (PENDING, ACCEPT)"),
                        fieldWithPath("data.content[].lastWorkoutPartnerRequest.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID (ACCEPT 상태일 때만 존재)").optional(),

                        *RestDocsPagination.paginationByCursor()
                    )
                )
            )
    }

    @DisplayName("찜 회원을 등록한다.")
    @Test
    fun favoriteAdd() {
        // given

        // when // then
        mockMvc.perform(
            post("/api/v1/favorites/members/{memberId}", 1L) // path variable 설정
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "favorite-add",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("memberId").description("찜 등록할 대상 회원 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("결과 데이터 (ok)")
                    )
                )
            )
    }

    @DisplayName("찜 회원을 삭제한다.")
    @Test
    fun favoriteDelete() {
        // given

        // when // then
        mockMvc.perform(
            delete("/api/v1/favorites/members/{memberId}", 1L) // path variable 설정
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document(
                    "favorite-delete",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("memberId").description("찜 취소할 대상 회원 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.STRING)
                            .description("결과 데이터 (ok)")
                    )
                )
            )
    }


}