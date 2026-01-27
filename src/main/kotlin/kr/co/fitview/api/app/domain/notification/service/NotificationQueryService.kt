package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationReadResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.QNotification.notification
import kr.co.fitview.api.app.domain.notification.registry.NotificationMapperRegistry
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class NotificationQueryService(
    private val notificationRepository : NotificationRepository,
    private val mapperRegistry: NotificationMapperRegistry,
    private val memberQueryService : MemberQueryService
) {

    fun findAllNotificationFrom(memberId: Long, condition : NotificationCondition) : Slice<NotificationResponse> {
        val findNotifications = notificationRepository.findAllNotificationBy(memberId, condition)

        val findMember = memberQueryService.findMemberFromId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val fromMemberIds = findNotifications.content
            .mapNotNull { it.fromMember!!.id!! }
            .toSet()
            .toList()

        val findFromMemberProfileMap = findNotificationMemberProfiles(fromMemberIds)
            .associateBy { it.memberId }

        val content = findNotifications.content.map {
            val sender = it.fromMember!!.id?.let { findFromMemberProfileMap[it] }
            mapperRegistry.map(it, findMember, sender!!)
        }

        return SliceImpl(content, findNotifications.pageable, findNotifications.hasNext())
    }

    fun findNotificationFrom(notificationId : Long) : Notification?{
        return notificationRepository.findByIdAndDeletedAtIsNull(notificationId)
    }

    fun findNotificationRead(memberId: Long): NotificationReadResponse {
        return notificationRepository.findNotificationReadBy(memberId)
    }

    fun findNotificationMemberProfiles(memberIds : List<Long>) : List<NotificationSender>{
        return notificationRepository.findNotificationProfilesByMemberIdIn(memberIds)
    }

}