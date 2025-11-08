package kr.co.fitview.api.app.global.exception.error.address

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class AddressErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVALID_SI_DO("001", "Invalid value for siDo", "주소의 시/도가 유효하지 않습니다."),
    MEMBER_ADDRESS_NOT_FOUND("002", "Member address not found", "해당 회원의 주소 정보가 존재하지 않습니다."),
    ADDRESS_NOT_FOUND("003", "Address not found", "해당 주소 정보가 존재하지 않습니다."),
    UNAUTHORIZED_ADDRESS_MODIFICATION("004", "Unauthorized address modification", "해당 회원이 소유하지 않은 주소를 수정하려고 시도했습니다."),
    NEGATIVE_RADIUS("005", "Negative radius value", "반경 값은 음수일 수 없습니다.")

    ;

    override val prefix: String
        get() = "ADDRESS"

}