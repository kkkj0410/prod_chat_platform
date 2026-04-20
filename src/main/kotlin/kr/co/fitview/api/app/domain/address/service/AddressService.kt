package kr.co.fitview.api.app.domain.address.service

import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.address.AddressErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class AddressService(
    private val addressRepository: AddressRepository,
    private val time: Time
) {

    fun addAddress(member: Member, request: AddressCreateServiceRequest): Address {

        val findAddresses = addressRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        findAddresses.forEach { address -> address.delete(time.nowLocalDateTime) }

        val address = Address.of(member, request)

        return addressRepository.save(address)
    }

    fun modifyAddress(memberId: Long, addressId: Long, request: AddressCreateServiceRequest): Address {
        val findAddress = addressRepository.findByIdAndMemberIdAndDeletedAtIsNull(
            addressId = addressId,
            memberId = memberId
        )

        if (isNull(findAddress)) {
            throw GlobalException(AddressErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }

        return findAddress!!.update(request)
    }

    private fun isNull(value: Any?) =
        value == null

}
