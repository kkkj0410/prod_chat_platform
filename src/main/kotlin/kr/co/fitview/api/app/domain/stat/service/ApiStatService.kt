package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ApiStatService(
    private val apiStatQueryService : ApiStatQueryService,
    private val apiStatRepository : ApiStatRepository,
    private val time : Time
) {

    @Transactional
    fun increaseApiStat(path : String, method : ApiStatMethod) : ApiStat {
        val statDate = time.nowLocalDate
        val normalizedPath = normalizePath(path)

        val findApiStat = apiStatQueryService.findApiStatFrom(
            statDate = statDate,
            path = normalizedPath,
            method = method
        )

        if(isNotNull(findApiStat)){
            findApiStat!!.increaseCount()
            return findApiStat
        }

        return try {
            apiStatRepository.save(
                ApiStat.of(
                    statDate = statDate,
                    path = normalizedPath,
                    method = method
                )
            )
        } catch (e: DataIntegrityViolationException) {
            val retry = apiStatQueryService.findApiStatFrom(
                statDate = statDate,
                path = normalizedPath,
                method = method
            ) ?: throw e

            retry.increaseCount()
            retry
        }
    }

    private fun isNotNull(data: Any?): Boolean = data != null

    private fun normalizePath(path: String): String {
        if (path == "/") return path
        return path.removeSuffix("/")
    }

}