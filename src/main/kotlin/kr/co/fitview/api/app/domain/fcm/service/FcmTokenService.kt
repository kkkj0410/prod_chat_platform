package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.fcm.FcmErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val memberQueryService: MemberQueryService
) {


    @Transactional
    fun saveFcmToken(memberId: Long, request: FcmTokenCreateServiceRequest): FcmToken {
        validateAlreadyExistsFcmToken(request.deviceId, request.token)

        val findMember =
            memberQueryService.findMemberFromId(memberId) ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val findFcmToken = fcmTokenRepository.findByDeviceIdAndIsActiveTrueAndDeletedAtIsNull(request.deviceId)

        if(isNotNull(findFcmToken)){
            findFcmToken!!.updateToken(request.token)
            return findFcmToken
        }

        val fcmToken = FcmToken.of(
            member = findMember,
            deviceId = request.deviceId,
            token = request.token,
            platform = request.platform
        )

        return fcmTokenRepository.save(fcmToken)
    }

    private fun validateAlreadyExistsFcmToken(deviceId : String, fcmToken : String) {
        fcmTokenRepository.findByActiveFcmTokenAndOtherDeviceId(
            deviceId = deviceId,
            fcmTokenString = fcmToken
        )?.let {
            throw GlobalException(FcmErrorCode.FCM_TOKEN_CONFLICT)
        }
    }

    @Transactional
    fun modifyAllFcmTokenFrom(deviceIds: List<String>) {
        fcmTokenRepository.deleteAllFcmTokenBy(deviceIds)
    }

    @Transactional
    fun deleteAllFcmFrom(memberId: Long) {
        fcmTokenRepository.deleteAllFcmTokenBy(memberId)
    }

    private fun isNotNull(value: Any?) = value != null


}