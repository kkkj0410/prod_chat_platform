package kr.co.fitview.api.app.domain.fcm.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.fcm.dto.response.FcmTokenActiveResponse
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.QFcmToken.fcmToken
import kr.co.fitview.api.app.domain.member.entity.QMember.member
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

    override fun deleteAllFcmTokenBy(memberId: Long) {

        queryFactory.update(fcmToken)
            .set(fcmToken.deletedAt, time.nowLocalDateTime)
            .where(
                fcmToken.member.id.eq(memberId),
                fcmToken.deletedAt.isNull
            )
            .execute()

        em.flush()
        em.clear()
    }

    override fun findByActiveFcmTokenAndOtherDeviceId(deviceId : String, fcmTokenString: String) : List<FcmToken>{
        return queryFactory
            .selectFrom(fcmToken)
            .where(
                fcmToken.deviceId.ne(deviceId),
                fcmToken.token.eq(fcmTokenString),
                fcmToken.deletedAt.isNull,
                fcmToken.isActive.isTrue
            )
            .fetch()
    }

    override fun findActiveFcmTokens(): List<FcmToken> {
        return queryFactory
            .selectFrom(fcmToken)
            .join(fcmToken.member, member).fetchJoin()
            .where(
                fcmToken.deletedAt.isNull,
                fcmToken.isActive.isTrue
            )
            .fetch()
    }


}