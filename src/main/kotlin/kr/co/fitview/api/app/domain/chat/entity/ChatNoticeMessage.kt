package kr.co.fitview.api.app.domain.chat.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.global.entity.BaseEntity

@Entity
@Table(name = "chat_notice_message")
class ChatNoticeMessage(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    var chatMessage: ChatMessage? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    var type: ChatNoticeMessageType? = null
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_notice_message_id", nullable = false)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_history_id")
    var workoutHistory: WorkoutHistory? = null

    companion object{
        fun ofWorkoutHistory(
            chatMessage : ChatMessage,
            workoutHistory : WorkoutHistory
        ) : ChatNoticeMessage {

            val newInstance = ChatNoticeMessage(
                chatMessage = chatMessage,
                type = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE
            )
            newInstance.workoutHistory = workoutHistory

            return newInstance
        }
    }

    val getWorkoutHistoryId: Long?
        get() = workoutHistory?.id
}