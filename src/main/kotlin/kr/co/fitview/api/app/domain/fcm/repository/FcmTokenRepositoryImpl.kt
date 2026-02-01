package kr.co.fitview.api.app.domain.fcm.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.QFcmToken.fcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.global.time.Time

class FcmTokenRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time,
    private val em : EntityManager
) : FcmTokenRepositoryCustom {


    override fun revokeAllFcmTokenBy(deviceIds: List<String>) {
        if (deviceIds.isEmpty()) return

        queryFactory.update(fcmToken)
            .set(fcmToken.status, FcmTokenStatus.REVOKED)
            .where(
                fcmToken.deviceId.`in`(deviceIds),
                fcmToken.status.eq(FcmTokenStatus.ACTIVE)
            )
            .execute()

        em.flush()
        em.clear()
    }

    override fun revokeAllFcmTokenBy(memberId: Long) {

        queryFactory.update(fcmToken)
            .set(fcmToken.status, FcmTokenStatus.REVOKED)
            .where(
                fcmToken.member.id.eq(memberId),
                fcmToken.status.eq(FcmTokenStatus.ACTIVE)
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
                fcmToken.status.eq(FcmTokenStatus.ACTIVE)
            )
            .fetch()
    }

    override fun findActiveFcmTokens(): List<FcmToken> {
        return queryFactory
            .selectFrom(fcmToken)
            .join(fcmToken.member, member).fetchJoin()
            .where(
                fcmToken.status.eq(FcmTokenStatus.ACTIVE)
            )
            .fetch()
    }


}