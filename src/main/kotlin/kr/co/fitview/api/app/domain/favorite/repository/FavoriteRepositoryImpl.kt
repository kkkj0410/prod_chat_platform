package kr.co.fitview.api.app.domain.favorite.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.entity.QFavorite.favorite
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class FavoriteRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : FavoriteRepositoryCustom {


    override fun findFavoriteMembers(
        memberId: Long,
        condition: FavoriteMemberCondition
    ): Slice<FavoriteMemberResponse> {

        val pageSize = condition.size

        val content = queryFactory
            .select(
                Projections.constructor(
                    FavoriteMemberResponse::class.java,
                    favorite.id,
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    image.url
                )
            )
            .from(favorite)
            .join(favorite.toMember, member)
            .join(member.mutableMemberImages, memberImage)
                .on(memberImage.type.eq(MemberImageType.PROFILE))
            .join(memberImage.image, image)
            .where(
                favorite.fromMember.id.eq(memberId),
                ltFavoriteId(condition.cursorFavoriteId),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .orderBy(favorite.id.desc())
            .limit((pageSize + 1).toLong())
            .fetch()

        val memberIds = content.map { it.memberId }


        val workoutImagesMap = if (memberIds.isNotEmpty()) {
            queryFactory
                .select(member.id, image.url)
                .from(memberImage)
                .join(memberImage.member, member)
                .join(memberImage.image, image)
                .where(
                    member.id.`in`(memberIds),
                    memberImage.type.eq(MemberImageType.WORKOUT),
                    memberImage.deletedAt.isNull
                )
                .fetch()
                .groupBy(
                    { it.get(member.id) },
                    { it.get(image.url) }
                )
        } else {
            emptyMap()
        }

        val resultResults = content.map { response ->
            val images = workoutImagesMap[response.memberId]
            response.copy(
                workoutImageUrl = images?.firstOrNull()
            )
        }.toMutableList()

        var hasNext = false
        if (resultResults.size > pageSize) {
            resultResults.removeAt(pageSize)
            hasNext = true
        }

        return SliceImpl(resultResults, PageRequest.of(0, pageSize), hasNext)
    }

    private fun ltFavoriteId(cursorFavoriteId: Long?): BooleanExpression? {
        return if (cursorFavoriteId == null) {
            null
        } else {
            favorite.id.lt(cursorFavoriteId)
        }
    }
}