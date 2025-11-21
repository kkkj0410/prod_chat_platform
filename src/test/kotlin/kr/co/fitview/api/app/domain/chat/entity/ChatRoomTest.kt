package kr.co.fitview.api.app.domain.chat.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomTest @Autowired constructor(
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("채팅방의 최신 메시지 갱신 시간을 기록한다.")
    @Test
    fun updateLastMessageAt() {
        // given
        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )

        // when
        chatRoom.updateLastMessageAt(time.nowLocalDateTime)

        // then
        assertThat(chatRoom)
            .extracting("type", "lastMessageAt")
            .contains(ChatRoomType.PRIVATE, time.nowLocalDateTime)

    }
}