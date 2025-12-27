package kr.co.fitview.api.app.domain.auth.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.auth.dto.response.RefreshTokenResponse
import kr.co.fitview.api.app.domain.auth.entity.QRefreshToken
import kr.co.fitview.api.app.domain.auth.entity.QRefreshToken.refreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.global.time.Time

class RefreshTokenRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time,
    private val em : EntityManager
) : RefreshTokenRepositoryCustom{


    override fun findAllExpiredRefreshToken() : List<RefreshTokenResponse> {

        val subRefreshToken = QRefreshToken("subRefreshToken")

        val expiredTokens = queryFactory
            .select(
                Projections.constructor(
                    RefreshTokenResponse::class.java,
                    refreshToken.uid,
                    refreshToken.deviceId
                )
            )
            .from(refreshToken)
            .where(
                refreshToken.deviceId.isNotNull,
                refreshToken.expiresAt.lt(time.nowLocalDateTime),
                refreshToken.status.eq(RefreshTokenStatus.ACTIVE),
                refreshToken.deletedAt.isNull,

                JPAExpressions.selectOne()
                    .from(subRefreshToken)
                    .where(
                        subRefreshToken.deviceId.eq(refreshToken.deviceId),
                        subRefreshToken.status.eq(RefreshTokenStatus.ACTIVE),
                        subRefreshToken.expiresAt.goe(time.nowLocalDateTime)
                    )
                    .notExists()
            )
            .fetch()

        return expiredTokens
    }

    override fun updateAllInactive(tokenIds: List<String>) {
        if (tokenIds.isEmpty()) return

        queryFactory.update(refreshToken)
            .set(refreshToken.status, RefreshTokenStatus.INACTIVE)
            .where(refreshToken.uid.`in`(tokenIds))
            .execute()

        em.flush()
        em.clear()
    }

}