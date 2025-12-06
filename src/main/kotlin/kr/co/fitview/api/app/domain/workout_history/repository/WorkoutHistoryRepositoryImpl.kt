package kr.co.fitview.api.app.domain.workout_history.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory

class WorkoutHistoryRepositoryImpl(
    private val queryFactory : JPAQueryFactory
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
}