package kr.co.fitview.api.app.domain.stat.aop

import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.service.ApiStatService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class ApiStatAspect(
    private val apiStatService: ApiStatService,
) {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    fun recordApiStat(joinPoint: ProceedingJoinPoint): Any? {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = requestAttributes?.request ?: return joinPoint.proceed()

        val rawMethod = request.method
        val rawPath = request.requestURI

        val normalizedPath = normalizePath(rawPath)

        val method = toApiStatMethod(rawMethod)
            ?: return joinPoint.proceed()

        val result = joinPoint.proceed()

        apiStatService.increaseApiStat(
            path = normalizedPath,
            method = method
        )

        return result
    }

    private fun toApiStatMethod(method: String): ApiStatMethod? {
        return runCatching {
            ApiStatMethod.valueOf(method.uppercase())
        }.getOrNull()
    }

    private fun normalizePath(path: String): String {
        return path
            .removeSuffix("/")
            .replace(Regex("/\\d+"), "/{id}")
    }
}