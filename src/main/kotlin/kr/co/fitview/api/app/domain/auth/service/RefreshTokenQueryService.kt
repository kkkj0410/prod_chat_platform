package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.domain.auth.dto.response.RefreshTokenResponse
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class RefreshTokenQueryService(
    val refreshTokenRepository : RefreshTokenRepository,
    val time : Time
) {

    fun findAllExpiredRefreshToken() : List<RefreshTokenResponse>{
        return refreshTokenRepository.findAllExpiredRefreshToken()
    }

}