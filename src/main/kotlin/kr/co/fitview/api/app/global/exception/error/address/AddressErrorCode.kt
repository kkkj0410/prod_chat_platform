package kr.co.fitview.api.app.global.exception.error.address

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class AddressErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVALID_SI_DO("001", "Invalid value for siDo", "주소의 시/도가 유효하지 않습니다.")



    ;

    override val prefix: String
        get() = "ADDRESS"

}