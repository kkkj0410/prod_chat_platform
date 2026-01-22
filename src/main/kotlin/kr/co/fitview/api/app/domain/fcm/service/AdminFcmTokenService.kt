package kr.co.fitview.api.app.domain.fcm.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import kr.co.fitview.api.app.domain.fcm.constant.DeepLinkConstant
import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.fcm.dto.response.FcmTokenActiveResponse
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter


@Service
@Transactional(readOnly = true)
class AdminFcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmPublisher: FcmPublisher,
) {


    fun push(request : FcmPushRequest){
        fcmPublisher.send(
            token = request.fcmToken!!,
            title = request.title,
            body = request.body,
            platform = request.platform!!,
            data = mapOf()
        )
    }

    fun findActiveFcmTokens() : List<FcmTokenActiveResponse>{
        return fcmTokenRepository.findActiveFcmTokens()
            .map { token ->
                FcmTokenActiveResponse(
                    email = token.member!!.email!!,
                    nickname = token.member!!.nickname!!,
                    fcmTokenId = token.id!!,
                    memberId = token.member!!.id!!,
                    createdAt = token.createdAt!!,
                    updatedAt = token.updatedAt!!,
                    deviceId = token.deviceId!!,
                    fcmToken = token.token!!,
                    platform = token.platform!!
                )
            }
    }

}