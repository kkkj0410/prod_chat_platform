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
@Transactional(readOnly = true)
class AddressService(
    private val addressRepository : AddressRepository,
    private val time : Time
) {

    @Transactional
    fun addAddress(member : Member, request : AddressCreateServiceRequest) : Address {

        val findAddresses = addressRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        findAddresses.forEach{ address -> address.delete(time.nowLocalDateTime)}

        val address = Address(
            member = member,
            siDo = convertToStandardSiDo(request.siDo),
            siGunGu = request.siGunGu,
            eupMyeonDong = request.eupMyeonDong,
            postalCode = request.postalCode,
            lat = request.lat,
            lng = request.lng,
            roadAddress = request.roadAddress
        )

        return addressRepository.save(address)
    }

    private fun convertToStandardSiDo(input: String): String {
        val mapping = mapOf(
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

        val matchedEntry = mapping.entries.firstOrNull { (key, _) ->
            input.contains(key)
        }

        return matchedEntry?.value
            ?: throw GlobalException(AddressErrorCode.INVALID_SI_DO)
    }
}