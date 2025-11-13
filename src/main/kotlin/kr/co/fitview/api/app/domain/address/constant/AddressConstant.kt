package kr.co.fitview.api.app.domain.address.constant

import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo

class AddressConstant private constructor() {

    companion object {
        val DEFAULT_SIDO: AddressSiDo = AddressSiDo.SEOUL
        const val DEFAULT_SIGUNGU: String = "서초구"
        const val DEFAULT_EUPMYEONDONG: String = "서초동"
        const val DEFAULT_LAT: Double = 37.4900861966502
        const val DEFAULT_LNG: Double = 127.01953478052
    }
}