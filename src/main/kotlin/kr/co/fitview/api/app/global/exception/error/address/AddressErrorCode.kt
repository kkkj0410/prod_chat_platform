package kr.co.fitview.api.app.global.exception.error.address

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class AddressErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    INVALID_SI_DO("001", "Invalid value for siDo", "주소의 시/도가 유효하지 않습니다."),
    INPUT_AND_ROAD_ADDRESS_BOTH_NULL("002", "Both inputAddress and roadAddress are null", "inputAddress와 roadAddress가 모두 비어 있습니다."),
    INPUT_AND_ROAD_ADDRESS_BOTH_PRESENT("003", "Both inputAddress and roadAddress are provided", "inputAddress와 roadAddress가 동시에 채워져 있습니다.");



    ;

    override val prefix: String
        get() = "ADDRESS"

}