package kr.co.fitview.api.app.global.security

import kr.co.fitview.api.app.global.entity.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val memberId : Long,
    val loginId : String,
    val role : Role,
) : UserDetails {

    private val authorities: MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority(role.toRoleName()))


    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return loginId
    }


}