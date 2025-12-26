package kr.co.fitview.api.app.domain.auth.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.auth.dto.response.RefreshTokenResponse
import kr.co.fitview.api.app.domain.auth.entity.QRefreshToken.refreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.global.time.Time

class RefreshTokenRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time,
    private val em : EntityManager
) : RefreshTokenRepositoryCustom{


    override fun findAllExpiredRefreshToken() : List<RefreshTokenResponse> {

        val expiredTokens = queryFactory
            .select(
                Projections.constructor(
                    RefreshTokenResponse::class.java,
                    refreshToken.id,
                    refreshToken.deviceId
                )
            )
            .from(refreshToken)
            .where(
                refreshToken.expiresAt.lt(time.nowLocalDateTime),
                refreshToken.status.eq(RefreshTokenStatus.ACTIVE),
                refreshToken.deletedAt.isNull
            )
            .fetch()

        return expiredTokens
    }

    override fun updateAllInactive(refreshTokenIds: List<String>) {
        if (refreshTokenIds.isEmpty()) return

        queryFactory.update(refreshToken)
            .set(refreshToken.status, RefreshTokenStatus.INACTIVE)
            .where(refreshToken.id.`in`(refreshTokenIds))
            .execute()

        em.flush()
        em.clear()
    }

}