package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
}