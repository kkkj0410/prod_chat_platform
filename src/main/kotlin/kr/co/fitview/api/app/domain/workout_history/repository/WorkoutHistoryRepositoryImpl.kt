package kr.co.fitview.api.app.domain.workout_history.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatNoticeMessage.chatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.QReviewReminderLog.reviewReminderLog
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryAndChatMessage
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.global.time.Time

class WorkoutHistoryRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time
) : WorkoutHistoryRepositoryCustom {


    override fun findWorkoutHistoryBy(chatRoomIds : List<Long>): List<WorkoutHistoryChatRoomResponse> {
        return queryFactory
            .select(
                Projections.constructor(
                    WorkoutHistoryChatRoomResponse::class.java,
                    chatRoom.id,
                    workoutHistory.id.max().isNotNull
                )
            )
            .from(chatRoom)
            .leftJoin(workoutHistory)
            .on(workoutHistory.chatRoom.id.eq(chatRoom.id))
            .groupBy(chatRoom.id)
            .fetch()
    }

    override fun findAllWorkoutHistoryExceed24HoursWithoutReview(): List<WorkoutHistoryAndChatMessage> {
        val twentyFourHoursAgo = time.nowLocalDateTime.minusHours(24)
        val threeMinutesAgo = time.nowLocalDateTime.minusMinutes(3)

        return queryFactory
            .select(
                Projections.constructor(
                    WorkoutHistoryAndChatMessage::class.java,
                    workoutHistory,
                    chatMessage
                )
            )
            .from(workoutHistory)
            .join(chatNoticeMessage)
            .on(chatNoticeMessage.workoutHistory.id.eq(workoutHistory.id))
            .join(chatNoticeMessage.chatMessage, chatMessage)
            .leftJoin(review)
            .on(review.workoutHistory.id.eq(workoutHistory.id))
            .leftJoin(reviewReminderLog)
            .on(
                reviewReminderLog.workoutHistory.id.eq(workoutHistory.id),
                reviewReminderLog.type.eq(ReviewReminderLogType.REVIEW_24H)
            )
            .where(
                chatNoticeMessage.type.eq(ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE),
//                workoutHistory.completedAt.loe(twentyFourHoursAgo),
                // 테스트를 위해 threeMinutesAgo로 해둠
                // 다시 twentyFourHoursAgo로 되돌려야함
                workoutHistory.completedAt.loe(threeMinutesAgo),
                review.id.isNull,
                reviewReminderLog.id.isNull,
                workoutHistory.deletedAt.isNull,
            )
            .fetch()
    }
}