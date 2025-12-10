package kr.co.fitview.api.app.docs.notification

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
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.controller.NotificationController
import kr.co.fitview.api.app.domain.notification.dto.response.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.service.NotificationQueryService
import kr.co.fitview.api.app.domain.notification.service.NotificationService
import kr.co.fitview.api.app.domain.review.controller.ReviewController
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
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


class NotificationControllerDocsTest : RestDocsSupport() {

    private val notificationQueryService: NotificationQueryService = mock(NotificationQueryService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return NotificationController(notificationQueryService, securityUtil)
    }

    @DisplayName("운동 파트너 요청 알림")
    @Test
    fun notificationList() {
        // given
        val condition = NotificationCondition(size = 10, lastSentAt = null)

        val notifications = listOf(
            NotificationResponse(
                notificationId = 1L,
                type = NotificationType.WORKOUT_PARTNER_REQUEST,
                sentAt = LocalDateTime.now(),
                isRead = true,
                sender = NotificationSender(123L, "호박", "https://..."),
                link = NotificationLink(LinkType.MEMBER_PROFILE, mapOf("memberId" to 123))
            ),
        )

        val slice: Slice<NotificationResponse> = SliceImpl(notifications, PageRequest.of(0, 10),false)

        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        // when // then
        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", condition.size.toString())
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)

            .andDo(
                document(
                    "notification-workout-partner-request",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각 (timestamp, 선택)")
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),

                        *RestDocsPagination.paginationByCursorAt(),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록 (Slice)"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters").type(JsonFieldType.OBJECT).description("링크 파라미터"),
                        fieldWithPath("data.content[].link.parameters.memberId").type(JsonFieldType.NUMBER).description("memberId를 이용해 링크 이동"),
                    )
                )
            )
    }

    @DisplayName("핏버디 수락 알림")
    @Test
    fun workoutPartnerAcceptNotification() {
        val notification = NotificationResponse(
            notificationId = 2L,
            type = NotificationType.WORKOUT_PARTNER_ACCEPT,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(124L, "멜론", "https://..."),
            link = NotificationLink(LinkType.CHAT_START, mapOf("memberId" to 124))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-partner-accept",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters").type(JsonFieldType.OBJECT).description("링크 파라미터"),
                        fieldWithPath("data.content[].link.parameters.memberId").type(JsonFieldType.NUMBER).description("memberId를 이용해 링크 이동"),
                        )
                )
            )
    }

    @DisplayName("운동 약속 알림")
    @Test
    fun workoutRequestNotification() {
        val notification = NotificationResponse(
            notificationId = 3L,
            type = NotificationType.WORKOUT_REQUEST,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(125L, "바나나", "https://..."),
            link = NotificationLink(LinkType.CHAT_ROOM, mapOf("chatRoomId" to 555))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-request",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID")
                    )
                )
            )
    }

    @DisplayName("운동 완료 알림")
    @Test
    fun workoutCompleteNotification() {
        val notification = NotificationResponse(
            notificationId = 4L,
            type = NotificationType.WORKOUT_COMPLETE,
            sentAt = LocalDateTime.now(),
            isRead = false,
            sender = NotificationSender(126L, "체리", "https://..."),
            link = NotificationLink(LinkType.REVIEW_WRITE, mapOf("workoutHistoryId" to 888, "chatRoomId" to 555))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-complete",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.workoutHistoryId").type(JsonFieldType.NUMBER).description("운동 기록 ID"),
                        fieldWithPath("data.content[].link.parameters.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID")
                    )
                )
            )
    }

    @DisplayName("후기 받음 알림")
    @Test
    fun reviewReceiveNotification() {
        val notification = NotificationResponse(
            notificationId = 5L,
            type = NotificationType.REVIEW_RECEIVE,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(127L, "포도", "https://..."),
            link = NotificationLink(LinkType.MEMBER_PROFILE_ME, null)
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-review-receive",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters")
                            .optional()
                            .type(JsonFieldType.OBJECT).description("링크 이동에 필요한 값")

                    )
                )
            )
    }

    @DisplayName("후기 요청 알림")
    @Test
    fun reviewRequestNotification() {
        val notification = NotificationResponse(
            notificationId = 6L,
            type = NotificationType.REVIEW_REQUEST,
            sentAt = LocalDateTime.now(),
            isRead = false,
            sender = NotificationSender(128L, "키위", "https://..."),
            link = NotificationLink(LinkType.REVIEW_WRITE, mapOf("workoutHistoryId" to 999, "chatRoomId" to 555))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-review-request",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.workoutHistoryId").type(JsonFieldType.NUMBER).description("운동 기록 ID"),
                        fieldWithPath("data.content[].link.parameters.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID")
                    )
                )
            )
    }

    @DisplayName("핏버디 거절 알림")
    @Test
    fun workoutPartnerRejectNotification() {
        val notification = NotificationResponse(
            notificationId = 7L,
            type = NotificationType.WORKOUT_PARTNER_REJECT,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(129L, "망고", "https://..."),
            link = NotificationLink(LinkType.MEMBER_PROFILE, mapOf("memberId" to 129))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-partner-reject",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.memberId").type(JsonFieldType.NUMBER).description("memberId를 이용해 링크 이동")
                    )
                )
            )
    }

    @DisplayName("운동 약속 수락 알림")
    @Test
    fun workoutRequestAcceptNotification() {
        val notification = NotificationResponse(
            notificationId = 8L,
            type = NotificationType.WORKOUT_REQUEST_ACCEPT,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(130L, "체리", "https://..."),
            link = NotificationLink(LinkType.CHAT_ROOM, mapOf("chatRoomId" to 777))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-request-accept",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID")
                    )
                )
            )
    }

    @DisplayName("운동 약속 거절 알림")
    @Test
    fun workoutRequestRejectNotification() {
        val notification = NotificationResponse(
            notificationId = 9L,
            type = NotificationType.WORKOUT_REQUEST_REJECT,
            sentAt = LocalDateTime.now(),
            isRead = true,
            sender = NotificationSender(131L, "바나나", "https://..."),
            link = NotificationLink(LinkType.CHAT_ROOM, mapOf("chatRoomId" to 888))
        )

        val slice: Slice<NotificationResponse> = SliceImpl(listOf(notification), PageRequest.of(0, 10), false)
        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/notifications")
                .header("Authorization", "Bearer jwt-token")
                .param("size", "10")
                .param("lastSentAt", "1765372519244")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andDo(
                document(
                    "notification-workout-request-reject",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),
                    queryParameters(
                        parameterWithName("size").description("(Optional - default 10) 페이지 사이즈"),
                        parameterWithName("lastSentAt").optional().description("(Optional)마지막 조회 시각")
                    ),
                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("알림 메시지 목록"),
                        fieldWithPath("data.content[].notificationId").type(JsonFieldType.NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("알림 타입"),
                        fieldWithPath("data.content[].sentAt").type(JsonFieldType.STRING).description("알림 발송 시각"),
                        fieldWithPath("data.content[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                        fieldWithPath("data.content[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
                        fieldWithPath("data.content[].sender.memberId").type(JsonFieldType.NUMBER).description("발신자 ID"),
                        fieldWithPath("data.content[].sender.nickname").type(JsonFieldType.STRING).description("발신자 닉네임"),
                        fieldWithPath("data.content[].sender.profileImageUrl").type(JsonFieldType.STRING).description("발신자 프로필 URL"),
                        fieldWithPath("data.content[].link").type(JsonFieldType.OBJECT).description("링크 정보"),
                        fieldWithPath("data.content[].link.type").type(JsonFieldType.STRING).description("링크 타입"),
                        fieldWithPath("data.content[].link.parameters.chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID")
                    )
                )
            )
    }
}