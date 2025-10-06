package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class MemberControllerTest @Autowired constructor(
    val securityUtil : SecurityUtil
) : ControllerTestSupport(){

    private fun setMemberFromSecurity() {
        val userPrincipal = UserPrincipal(1L, Role.USER)
        val newAuthentication = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
        SecurityContextHolder.getContext().authentication = newAuthentication
    }


    @DisplayName("인증된 jwt 토큰으로 회원 정보를 조회한다.")
    @Test
    fun memberMe() {
        // given
        setMemberFromSecurity()

        // when // then
        mockMvc.perform(
            get("/api/v1/members/me")
            .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
    }

    @DisplayName("인증된 jwt 토큰이 없다면 회원 정보를 조회하지 못한다.")
    @Test
    fun memberMeWithoutAuthentication() {

        // when // then
        mockMvc.perform(
            get("/api/v1/members/me")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(JwtErrorCode.JWT_TOKEN_INVALID.code))
            .andExpect(jsonPath("$.status").value("401"))
            .andExpect(jsonPath("$.message").value(JwtErrorCode.JWT_TOKEN_INVALID.message))
            .andExpect(jsonPath("$.data").isEmpty())
    }

}