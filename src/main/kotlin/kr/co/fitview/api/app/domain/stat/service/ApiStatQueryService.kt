package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
@Transactional(readOnly = true)
class ApiStatQueryService(
    private val apiStatRepository : ApiStatRepository
) {

    fun findApiStatFrom(statDate : LocalDate, path : String, method : ApiStatMethod) : ApiStat? {
        return apiStatRepository.findByStatDateAndPathAndMethod(statDate, path, method)
    }

    fun findTopApiStatFrom(statDate : LocalDate, limit : Int) : List<ApiStat>{
        return apiStatRepository.findApiStatTop(statDate, limit)
    }


}