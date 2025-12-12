package kr.co.fitview.api.app.domain.notification.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import java.time.LocalDateTime

class NotificationControllerTest : ControllerTestSupport(){


    @DisplayName("회원의 알림 메시지를 조회한다.")
    @Test
    fun notificationList() {
        // given
        val memberId = 1L
        val condition = NotificationCondition(size = 10, lastSentAt = null)

        val notifications = listOf(
            NotificationResponse(
                notificationId = 1L,
                type = NotificationType.WORKOUT_PARTNER_REQUEST,
                sentAt = LocalDateTime.now(),
                isRead = true,
                sender = NotificationSender(123L, "호박", "https://..."),
                link = NotificationLink(LinkType.MEMBER_PROFILE, mapOf("memberId" to 123)),
                messages = NotificationMessage(
                    text1 = NotificationType.WORKOUT_PARTNER_REQUEST.displayText1,
                    text2 = NotificationType.WORKOUT_PARTNER_REQUEST.displayText2,
                )
            ),
            NotificationResponse(
                notificationId = 2L,
                type = NotificationType.WORKOUT_PARTNER_ACCEPT,
                sentAt = LocalDateTime.now().minusMinutes(5),
                isRead = true,
                sender = NotificationSender(124L, "멜론", "https://..."),
                link = NotificationLink(LinkType.CHAT_START, mapOf("memberId" to 124)),
                messages = NotificationMessage(
                    text1 = NotificationType.WORKOUT_COMPLETE.displayText1,
                    text2 = NotificationType.WORKOUT_COMPLETE.displayText2,
                )
            ),
            NotificationResponse(
                notificationId = 3L,
                type = NotificationType.WORKOUT_COMPLETE,
                sentAt = LocalDateTime.now().minusHours(1),
                isRead = false,
                sender = NotificationSender(125L, "감자", "https://..."),
                link = NotificationLink(LinkType.REVIEW_WRITE, mapOf("workoutHistoryId" to 456, "chatRoomId" to 789)),
                messages = NotificationMessage(
                    text1 = NotificationType.WORKOUT_COMPLETE.displayText1,
                    text2 = NotificationType.WORKOUT_COMPLETE.displayText2,
                )
            )
        )

        val slice: Slice<NotificationResponse> = SliceImpl(notifications, PageRequest.of(0, 10), false)

        given(notificationQueryService.findAllNotificationFrom(any(), any())).willReturn(slice)


        // when / then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("memberId", memberId.toString())
                .param("size", condition.size.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content[0].notificationId").value(1))
            .andExpect(jsonPath("$.data.content[0].type").value("WORKOUT_PARTNER_REQUEST"))
            .andExpect(jsonPath("$.data.content[0].isRead").value(true))
            .andExpect(jsonPath("$.data.content[0].sender.memberId").value(123))
            .andExpect(jsonPath("$.data.content[0].link.type").value("MEMBER_PROFILE"))
            .andExpect(jsonPath("$.data.content[1].notificationId").value(2))
            .andExpect(jsonPath("$.data.content[1].type").value("WORKOUT_PARTNER_ACCEPT"))
            .andExpect(jsonPath("$.data.content[1].link.type").value("CHAT_START"))
            .andExpect(jsonPath("$.data.content[2].notificationId").value(3))
            .andExpect(jsonPath("$.data.content[2].type").value("WORKOUT_COMPLETE"))
            .andExpect(jsonPath("$.data.content[2].link.type").value("REVIEW_WRITE"))
    }

    @DisplayName("회원의 알람 메시지를 읽음 처리한다.")
    @Test
    fun notificationReadModify() {

        // when / then
        mockMvc.perform(
            patch("/api/v1/notifications/{notificationId}/read", 123)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
    }



    @DisplayName("회원의 알림 메시지를 조회한다.")
    @Test
    fun notificationRead() {
        // given
        given(notificationQueryService.findNotificationRead(any())).willReturn(
            NotificationReadResponse(
                isUnreadNotificationExists = true
            )
        )


        // when / then
        mockMvc.perform(
            get("/api/v1/notifications/read")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.isUnreadNotificationExists").value(true))
    }
}