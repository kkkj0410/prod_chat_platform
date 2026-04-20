package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AddressQueryService(
    private val addressRepository: AddressRepository
) {

    fun findAddressFromMemberId(memberId: Long): AddressResponse {
        val findAddress = addressRepository.findByMemberIdAndDeletedAtIsNull(memberId)
        if (isNull(findAddress)) {
            throw GlobalException(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }

        if (isNotSeoul(findAddress!!)){
            return AddressResponse.fromDefault(findAddress.id!!)
        }

        return AddressResponse.from(findAddress)
    }

    fun findAddressEntityFrom(memberId : Long) : Address?{
        return addressRepository.findByMemberIdAndDeletedAtIsNull(memberId)
    }

    fun findAddressFromAddressId(addressId: Long): AddressDetailResponse {
        val findAddress = addressRepository.findByIdAndDeletedAtIsNull(addressId)
        if (isNull(findAddress)) {
            throw GlobalException(AddressErrorCode.ADDRESS_NOT_FOUND)
        }

        if (isNotSeoul(findAddress!!)){
            return AddressDetailResponse.fromDefault(findAddress.id!!)
        }

        return AddressDetailResponse.from(findAddress)
    }

    private fun isNull(value: Any?) =
        value == null

    private fun isNotSeoul(findAddress: Address) =
        findAddress.siDo != AddressConstant.DEFAULT_SIDO
}
