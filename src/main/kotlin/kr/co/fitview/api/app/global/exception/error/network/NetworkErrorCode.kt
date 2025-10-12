package kr.co.fitview.api.app.global.exception.error.network

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class NetworkErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    NETWORK_SEND_ERROR("001", "send error", "BE 서버가 외부 플랫폼과 통신에 실패"),


    ;

    override val prefix: String
        get() = "NETWORK"

}