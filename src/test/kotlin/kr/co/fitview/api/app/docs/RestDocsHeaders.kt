package kr.co.fitview.api.app.docs

import kr.co.fitview.api.app.global.entity.Role
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName

object RestDocsHeaders {
    const val AUTHORIZATION = "Authorization"

    fun authorizationHeader(requiredRole: Role): HeaderDescriptor {
        return headerWithName(AUTHORIZATION)
            .description("Bearer 토큰 (예: 'Bearer {accessToken}') - 필요 권한: $requiredRole")
    }
}