package kr.co.fitview.api.app.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.SecurityAuthenticationException
import kr.co.fitview.api.app.global.exception.error.security.SecurityErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class SecurityExceptionFilter : OncePerRequestFilter() {


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
       try {
            filterChain.doFilter(request, response);
        } catch (ex : GlobalException) {
           response.contentType = "application/json;charset=UTF-8"
           response.characterEncoding = "UTF-8"

           val errorCode = ex.errorCode

           val apiResponse = errorCode.toApiResponse()

           val objectMapper = ObjectMapper()
           response.status = HttpStatus.UNAUTHORIZED.value()
           response.writer.write(objectMapper.writeValueAsString(apiResponse))
           response.writer.flush()
       }

    }
}