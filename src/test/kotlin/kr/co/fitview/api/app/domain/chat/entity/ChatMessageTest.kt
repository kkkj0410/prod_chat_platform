package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageNoticeContent
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.annotations.ColumnDefault
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class ChatMessageTest @Autowired constructor(
    val time : Time
): IntegrationTestSupport(){


    @DisplayName("TEXT 메시지를 생성한다.")
    @Test
    fun ofText() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        //when
        val chatMessage = ChatMessage.ofText(
            member = member,
            chatRoom = chatRoom,
            content = "content",
            sentAt = time.nowLocalDateTime
        )

        // then
        assertThat(chatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(member, chatRoom, ChatMessageType.TEXT, "content", time.nowLocalDateTime)
    }

    @DisplayName("WORKOUT_REQUEST 메시지를 생성한다.")
    @Test
    fun ofWorkoutRequest() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        //when
        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = member,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )

        // then
        assertThat(chatMessage)
            .extracting("member", "chatRoom", "type", "sentAt")
            .contains(member, chatRoom, ChatMessageType.WORKOUT_REQUEST, time.nowLocalDateTime)

    }

    @DisplayName("안내 문구 유형의 메시지를 생성한다.")
    @Test
    fun ofNotice() {
        // given
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        //when
        val chatMessage = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            content = ChatMessageNoticeContent.WORKOUT_REQUEST_CANCEL,
            sentAt = time.nowLocalDateTime
        )

        // then
        assertThat(chatMessage)
            .extracting("chatRoom", "type", "content", "sentAt")
            .contains(chatRoom, ChatMessageType.NOTICE, "WORKOUT_REQUEST_CANCEL", time.nowLocalDateTime)
    }
}