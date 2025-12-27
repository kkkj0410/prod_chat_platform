package kr.co.fitview.api.app.domain.auth.repository

import kr.co.fitview.api.app.domain.auth.dto.response.RefreshTokenResponse

interface RefreshTokenRepositoryCustom{

    fun findAllExpiredRefreshToken() : List<RefreshTokenResponse>

    fun updateAllInactive(tokenIds: List<String>)

}