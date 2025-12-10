package kr.co.fitview.api.app.domain.notification.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.QNotification.notification
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
                lastSentAtCondition
            )
            .orderBy(notification.sentAt.desc())
            .limit(condition.size + 1L)
            .fetch()

        val hasNext = query.size > condition.size
        val content = if (hasNext) query.subList(0, condition.size) else query

        return SliceImpl(content, PageRequest.of(0, condition.size), hasNext)
    }


}