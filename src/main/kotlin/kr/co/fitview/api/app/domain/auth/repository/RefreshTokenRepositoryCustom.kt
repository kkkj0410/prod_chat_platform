package kr.co.fitview.api.app.domain.auth.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.auth.dto.response.RefreshTokenResponse
import kr.co.fitview.api.app.global.time.Time

interface RefreshTokenRepositoryCustom{

    fun findAllExpiredRefreshToken() : List<RefreshTokenResponse>

    fun updateAllInactive(refreshTokenIds: List<String>)

}