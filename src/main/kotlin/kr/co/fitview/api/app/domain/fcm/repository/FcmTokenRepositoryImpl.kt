package kr.co.fitview.api.app.domain.fcm.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.fcm.entity.QFcmToken.fcmToken
import kr.co.fitview.api.app.global.time.Time

class FcmTokenRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time,
    private val em : EntityManager
) : FcmTokenRepositoryCustom {


    override fun deleteAllFcmTokenBy(deviceIds: List<String>) {
        if (deviceIds.isEmpty()) return

        queryFactory.update(fcmToken)
            .set(fcmToken.deletedAt, time.nowLocalDateTime)
            .where(
                fcmToken.deviceId.`in`(deviceIds),
                fcmToken.deletedAt.isNull
            )
            .execute()

        em.flush()
        em.clear()
    }


}