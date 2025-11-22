package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MessageReadStatusServiceTest @Autowired constructor(
    private val messageReadStatusService : MessageReadStatusService,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원이 메시지를 보낼 시, 채팅방에 있는 모든 회원에 대한 메시지 읽음 여부를 저장한다.")
    @Test
    fun saveMessageReadStatus() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage)

        // when
        messageReadStatusService.saveMessageReadStatus(
            member = me,
            chatMessage = chatMessage,
            chatRoom = savedChatRoom,
        )

        // then
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, chatMessage, true),
                tuple(savedChatRoom, other, chatMessage, false),
            )
    }
}