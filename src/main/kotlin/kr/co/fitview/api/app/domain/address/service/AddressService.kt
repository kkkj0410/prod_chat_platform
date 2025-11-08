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

//        validateIsNullAllAddress(request)

//        validateIsNotNullAllAddress(request)

        val findAddresses = addressRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        findAddresses.forEach{ address -> address.delete(time.nowLocalDateTime)}

        val address = Address(
            member = member,
            siDo = convertToStandardSiDo(request.siDo),
            siGunGu = request.siGunGu,
            eupMyeonDong = request.eupMyeonDong,
            lat = request.lat,
            lng = request.lng,
            fullAddress = request.fullAddress
        )

        return addressRepository.save(address)
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
            throw GlobalException(AddressErrorCode.ADDRESS_ID_NOT_FOUND)
        }

        return AddressDetailResponse.from(findAddress!!)
    }

    private fun isNull(value : Any?) =
        value == null
//
//    private fun isNotNull(value : Any?) =
//        value != null

    private fun convertToStandardSiDo(inputSiDo: String?): String? {
        if(inputSiDo == null){
            return null
        }

        val siDoMap = mapOf(
            "서울" to "서울특별시",
            "부산" to "부산광역시",
            "인천" to "인천광역시",
            "대구" to "대구광역시",
            "대전" to "대전광역시",
            "광주" to "광주광역시",
            "울산" to "울산광역시",
            "세종" to "세종특별자치시",
            "경기" to "경기도",
            "충북" to "충청북도",
            "충남" to "충청남도",
            "전남" to "전라남도",
            "전북" to "전라북도",
            "경북" to "경상북도",
            "경남" to "경상남도",
            "강원" to "강원특별자치도",
            "제주" to "제주특별자치도"
        )

        if(containsFullSiDo(inputSiDo, siDoMap)){
            return getFullSiDo(inputSiDo, siDoMap)
        }

        if(containsKeySiDo(inputSiDo, siDoMap)){
            return getValueSiDoByKey(inputSiDo, siDoMap)
        }

        if(containsKeyFuzzySiDo(inputSiDo, siDoMap)){
            return getKeyFuzzySiDoValue(inputSiDo, siDoMap)
        }

        throw GlobalException(AddressErrorCode.INVALID_SI_DO)
    }

    private fun containsFullSiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
        return siDoMap.values.any { inputSiDo.contains(it) }
    }

    private fun getFullSiDo(inputSiDo: String, siDoMap: Map<String, String>): String {
        return siDoMap.values.first { inputSiDo.contains(it) }
    }

    private fun containsKeySiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
        return siDoMap.keys.any { inputSiDo.contains(it) }
    }

    private fun getValueSiDoByKey(inputSiDo: String, siDoMap: Map<String, String>): String {
        val matchedKey = siDoMap.keys.first { inputSiDo.contains(it) }
        return siDoMap[matchedKey]!!
    }

    private fun containsKeyFuzzySiDo(inputSiDo: String, siDoMap: Map<String, String>): Boolean {
        return siDoMap.keys.any { key ->
            val pattern = key.toCharArray().joinToString(".*") { Regex.escape(it.toString()) }
            Regex(pattern, RegexOption.DOT_MATCHES_ALL).containsMatchIn(inputSiDo)
        }
    }

    private fun getKeyFuzzySiDoValue(inputSiDo: String, siDoMap: Map<String, String>): String {
        val entry = siDoMap.entries.first { (key, _) ->
            val pattern = key.toCharArray().joinToString(".*") { Regex.escape(it.toString()) }
            Regex(pattern, RegexOption.DOT_MATCHES_ALL).containsMatchIn(inputSiDo)
        }
        return entry.value
    }


}