package kr.co.fitview.api.app.global.exception

import kr.co.fitview.api.app.global.exception.error.ErrorCode

class GlobalException(
    val errorCode : ErrorCode
) : RuntimeException(){
}