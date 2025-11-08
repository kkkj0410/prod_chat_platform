package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class AddressService(
    private val addressRepository : AddressRepository,
    private val time : Time
) {

    @Transactional
    fun addAddress(member : Member, request : AddressCreateServiceRequest) : Address {

        val findAddresses = addressRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        findAddresses.forEach{ address -> address.delete(time.nowLocalDateTime)}

        val address = Address.of(member, request)

        return addressRepository.save(address)
    }

    @Transactional
    fun modifyAddress(memberId : Long, addressId: Long, request: AddressCreateServiceRequest) : Address {
        val findAddress = addressRepository.findByIdAndMemberIdAndDeletedAtIsNull(
            addressId = addressId,
            memberId = memberId
        )

        if(isNull(findAddress)){
            throw GlobalException(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }

        return findAddress!!.update(request)
    }

    fun findAddressFromMemberId(memberId: Long) : AddressResponse{
        val findAddress = addressRepository.findByMemberIdAndDeletedAtIsNull(memberId)
        if(isNull(findAddress)){
            throw GlobalException(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }

        return AddressResponse.from(findAddress!!)
    }

    fun findAddressFromAddressId(addressId: Long): AddressDetailResponse {
        val findAddress = addressRepository.findByIdAndDeletedAtIsNull(addressId)
        if(isNull(findAddress)){
            throw GlobalException(AddressErrorCode.ADDRESS_NOT_FOUND)
        }

        return AddressDetailResponse.from(findAddress!!)
    }

    private fun isNull(value : Any?) =
        value == null



}