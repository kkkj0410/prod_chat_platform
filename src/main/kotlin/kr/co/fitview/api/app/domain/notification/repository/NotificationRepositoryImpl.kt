package kr.co.fitview.api.app.domain.notification.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationReadResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.QNotification.notification
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.Instant
import java.time.ZoneId

class NotificationRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : NotificationRepositoryCustom {


    override fun findAllNotificationBy(memberId: Long, condition : NotificationCondition): Slice<Notification> {

        val lastSentAtCondition = condition.lastSentAt?.let {
            notification.sentAt.lt(
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
        }

        val query = queryFactory
            .selectFrom(notification)
            .where(
                notification.member.id.eq(memberId),
                lastSentAtCondition,
                notification.fromMember.deletedAt.isNull,
            )
            .orderBy(notification.sentAt.desc())
            .limit(condition.size + 1L)
            .fetch()

        val hasNext = query.size > condition.size
        val content = if (hasNext) query.subList(0, condition.size) else query

        return SliceImpl(content, PageRequest.of(0, condition.size), hasNext)
    }

    override fun findNotificationReadBy(memberId: Long): NotificationReadResponse {
        val isUnreadExists  =  queryFactory
            .select(notification.id)
            .from(notification)
            .where(
                notification.member.id.eq(memberId),
                notification.isRead.eq(false)
            )
            .fetchFirst() != null

        return NotificationReadResponse(isUnreadExists)
    }

    override fun findNotificationProfilesByMemberIdIn(memberIds: List<Long>): List<NotificationSender> {
        return queryFactory
            .select(
                Projections.constructor(
                    NotificationSender::class.java,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .on(memberImage.type.eq(MemberImageType.PROFILE))
            .join(memberImage.image, image)
            .where(
                member.id.`in`(memberIds),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .fetch()
    }


}