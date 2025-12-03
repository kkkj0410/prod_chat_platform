package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

    fun findByType(type: ChatMessageType): List<ChatMessage>
}