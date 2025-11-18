package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatParticipantRepository : JpaRepository<ChatParticipant, Long> {
}