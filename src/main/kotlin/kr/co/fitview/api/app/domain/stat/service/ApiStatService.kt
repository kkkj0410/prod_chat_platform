package kr.co.fitview.api.app.domain.stat.service

import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ApiStatService(
    private val apiStatRepository : ApiStatRepository
) {




}