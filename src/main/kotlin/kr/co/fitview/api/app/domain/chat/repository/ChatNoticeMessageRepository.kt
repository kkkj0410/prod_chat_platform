package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatNoticeMessageRepository : JpaRepository<ChatNoticeMessage, Long> {
}