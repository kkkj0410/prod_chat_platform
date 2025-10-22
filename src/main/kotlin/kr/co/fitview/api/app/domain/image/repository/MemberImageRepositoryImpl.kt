package kr.co.fitview.api.app.domain.image.repository

import com.querydsl.jpa.impl.JPAQueryFactory

class MemberImageRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberImageRepositoryCustom{

    override fun findWithImageByMemberIdAndProfileAndDeletedAtIsNull(memberId: Long) {

//        QMemberImage

//        queryFactory
//            .select(QMemberImage.memberImage)

        TODO("Not yet implemented")
    }
}