package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageNoticeContent
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatNoticeService(
    private val chatRoomQueryService : ChatRoomQueryService,
    private val chatMessageRepository : ChatMessageRepository,
    private val time : Time
) {


    @Transactional
    fun addChatNoticeFrom(workoutRequestId: Long, content : ChatMessageNoticeContent) : ChatMessage {

        val findChatRoom = chatRoomQueryService.findChatRoomFrom(workoutRequestId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val chatMessage = ChatMessage.ofNotice(
            chatRoom = findChatRoom,
            content = content,
            sentAt = time.nowLocalDateTime
        )

        return chatMessageRepository.save(chatMessage)
    }


}