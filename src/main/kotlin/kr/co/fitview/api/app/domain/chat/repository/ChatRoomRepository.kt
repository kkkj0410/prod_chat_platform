package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
}