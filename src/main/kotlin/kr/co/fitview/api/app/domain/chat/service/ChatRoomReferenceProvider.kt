//package kr.co.fitview.api.app.domain.chat.service
//
//import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
//import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
//import org.springframework.stereotype.Component
//
//
//@Component
//class ChatRoomReferenceProvider(
//    private val chatRoomRepository : ChatRoomRepository
//) {
//
//    fun findChatRoomReferenceFrom(chatRoomId : Long) : ChatRoom {
//        return chatRoomRepository.getReferenceById(chatRoomId)
//    }
//}