package kr.co.fitview.api.app.domain.auth.repository

import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, String>, RefreshTokenRepositoryCustom