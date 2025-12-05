package kr.co.fitview.api.app.domain.workout.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest

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