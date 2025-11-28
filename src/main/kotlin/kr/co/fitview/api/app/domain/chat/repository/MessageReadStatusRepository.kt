package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.domain.chat.entity.MessageReadStatus
import org.springframework.data.jpa.repository.JpaRepository

interface MessageReadStatusRepository : JpaRepository<MessageReadStatus, Long>, MessageReadStatusRepositoryCustom {
}