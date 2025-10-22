package kr.co.fitview.api.app.domain.image.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.image.entity.MemberImage
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType

class MemberImageRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberImageRepositoryCustom{

    override fun findWithImageByMemberIdAndProfileAndDeletedAtIsNull(memberId: Long) : MemberImage? {
        return queryFactory
            .selectFrom(memberImage)
            .join(memberImage.image).fetchJoin()
            .where(
                memberImage.member.id.eq(memberId),
                memberImage.type.eq(MemberImageType.PROFILE),
                memberImage.deletedAt.isNull()
            )
            .fetchOne()
    }

    override fun findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(memberId: Long): List<MemberImage> {
        return queryFactory
            .selectFrom(memberImage)
            .join(memberImage.image).fetchJoin()
            .where(
                memberImage.member.id.eq(memberId),
                memberImage.type.eq(MemberImageType.WORKOUT),
                memberImage.deletedAt.isNull()
            )
            .fetch()
    }
}