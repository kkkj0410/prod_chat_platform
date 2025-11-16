package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.address.entity.QAddress
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.QImage
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationResponse
import kr.co.fitview.api.app.domain.member.dto.response.QMemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.QMemberRecommendationResponse
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle


class MemberRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val em: EntityManager
) : MemberRepositoryCustom {



    override fun findAllMemberIdWithinRecommendation(memberId: Long): List<Long> {

        val subMember = QMember("subMember")

//        val builder = BooleanBuilder()
//
//        builder.or(member.workoutExperience.eq(subMember.workoutExperience)
//            .and(member.workoutStyle.eq(subMember.workoutStyle)))
//
//        builder.or(member.workoutExperience.eq(subMember.workoutExperience)
//            .and(member.workoutGoal.eq(subMember.workoutGoal)))
//
//        builder.or(member.workoutStyle.eq(subMember.workoutStyle)
//            .and(member.workoutGoal.eq(subMember.workoutGoal)))

//        val twoOfThreeMatch = (
//            member.workoutExperience.eq(subMember.workoutExperience)
//                .and(member.workoutStyle.eq(subMember.workoutStyle))
//            ).or(
//            member.workoutExperience.eq(subMember.workoutExperience)
//                .and(member.workoutGoal.eq(subMember.workoutGoal))
//            ).or(
//                member.workoutStyle.eq(subMember.workoutStyle)
//                    .and(member.workoutGoal.eq(subMember.workoutGoal))
//            )

//        val matchCount = Expressions.numberTemplate(
//            Integer::class.java,
//            "(CASE WHEN {0} = {1} THEN 1 ELSE 0 END) + " +
//                    "(CASE WHEN {2} = {3} THEN 1 ELSE 0 END) + " +
//                    "(CASE WHEN {4} = {5} THEN 1 ELSE 0 END)",
//            member.workoutExperience, subMember.workoutExperience,
//            member.workoutStyle, subMember.workoutStyle,
//            member.workoutGoal, subMember.workoutGoal
//        )
//
//
//        val twoOfThreeMatch = matchCount.goe(2)
//
//        val subQueryAddress = JPAExpressions
//            .selectOne()
//            .from(address)
//            .join(subMember).on(subMember.id.eq(memberId))
//            .where(
//                address.siDo.eq("서울특별시"),
//                twoOfThreeMatch
//            )
//            .limit(1)


        val matchCount = Expressions.numberTemplate(
            Integer::class.java,
            "(CASE WHEN {0} = {1} THEN 1 ELSE 0 END) + " +
                    "(CASE WHEN {2} = {3} THEN 1 ELSE 0 END) + " +
                    "(CASE WHEN {4} = {5} THEN 1 ELSE 0 END)",
            member.workoutExperience, subMember.workoutExperience,
            member.workoutStyle, subMember.workoutStyle,
            member.workoutGoal, subMember.workoutGoal
        )

        return queryFactory
            .select(member.id)
            .from(member)
            .join(address).on(address.member.eq(member))
            .join(subMember).on(subMember.id.eq(memberId))
            .where(
                address.siDo.eq(AddressSiDo.SEOUL),
                matchCount.goe(2)
            )
            .fetch()

//        val sql = """
//        SELECT
//            m.member_id,
//            m.nickname,
//            m.workout_experience,
//            m.workout_style,
//            m.workout_goal,
//            MAX(CASE WHEN ranked.type = 'PROFILE' THEN i.url END) AS profile_image_url,
//            MAX(CASE WHEN ranked.type = 'WORKOUT' THEN i.url END) AS workout_image_url
//        FROM member m
//        LEFT JOIN (
//            SELECT
//                mi.member_id,
//                mi.type,
//                mi.image_id
//            FROM (
//                SELECT
//                    member_id,
//                    type,
//                    image_id,
//                    ROW_NUMBER() OVER (PARTITION BY member_id, type ORDER BY seq ASC) AS rn
//                FROM member_image
//                WHERE type IN ('PROFILE', 'WORKOUT')
//            ) mi
//            WHERE mi.rn = 1
//        ) ranked ON ranked.member_id = m.member_id
//        LEFT JOIN image i ON i.image_id = ranked.image_id
//        WHERE m.member_id IN (:memberIds)
//        GROUP BY m.member_id
//        $orderBy
//    """.trimIndent()
    }

//    override fun findRecommendationMemberByIdIn(memberIds: List<Long>): List<MemberRecommendationResponse> {
//
////        val profileImage = QImage("profileImage")
////        val workoutImage = QImage("workoutImage")
//////
//////        val profileImageUrlSubquery = JPAExpressions
//////            .select(profileImage.url)
//////            .from(profileImage)
//////            .join(memberImage).on(memberImage.image.id.eq(profileImage.id))
//////            .where(
//////                memberImage.member.id.eq(member.id),
//////                memberImage.type.eq(MemberImageType.PROFILE)
//////            )
//////            .limit(1)
//////
//////        val workoutImageUrlSubquery = JPAExpressions
//////            .select(workoutImage.url)
//////            .from(workoutImage)
//////            .join(memberImage).on(memberImage.image.id.eq(workoutImage.id))
//////            .where(
//////                memberImage.member.id.eq(member.id),
//////                memberImage.type.eq(MemberImageType.WORKOUT),
//////            )
//////            .orderBy(memberImage.seq.asc())
//////
//////
//////
//////        return queryFactory
//////            .select(
//////                QMemberRecommendationResponse(
//////                    member.id,
//////                    member.nickname,
//////                    member.workoutExperience,
//////                    member.workoutStyle,
//////                    member.workoutGoal,
//////                    profileImageUrlSubquery,
//////                    workoutImageUrlSubquery
//////                )
//////            )
//////            .from(member)
//////            .where(member.id.`in`(memberIds))
//////            .fetch()
////
////
////        return queryFactory
////            .select(
////                QMemberRecommendationResponse(
////                    member.id,
////                    member.nickname,
////                    member.workoutExperience,
////                    member.workoutStyle,
////                    member.workoutGoal,
////                    profileImage.url,
////                    workoutImage.url
////                )
////            )
////            .from(member)
////            .leftJoin(memberImage).on(memberImage.member.eq(member))
////            .leftJoin(profileImage).on(profileImage.id.eq(memberImage.image.id)
////                .and(memberImage.type.eq(MemberImageType.PROFILE)))
////            .leftJoin(workoutImage).on(workoutImage.id.eq(memberImage.image.id)
////                .and(memberImage.type.eq(MemberImageType.WORKOUT)))
////            .where(member.id.`in`(memberIds))
////            .groupBy(member.id)
////            .orderBy(memberImage.seq.min().asc())
////            .fetch()
//
////        println(tuples)
//
//        val sql = """
//        SELECT
//            m.member_id,
//            MAX(CASE WHEN ranked.type = 'PROFILE' THEN i.url END) AS profile_image_url,
//            MAX(CASE WHEN ranked.type = 'WORKOUT' THEN i.url END) AS workout_image_url
//        FROM member m
//        LEFT JOIN (
//            SELECT
//                mi.member_id,
//                mi.type,
//                mi.image_id
//            FROM (
//                SELECT
//                    member_id,
//                    type,
//                    image_id,
//                    ROW_NUMBER() OVER (PARTITION BY member_id, type ORDER BY seq ASC) AS rn
//                FROM member_image
//                WHERE type IN ('PROFILE', 'WORKOUT')
//            ) mi
//            WHERE mi.rn = 1
//        ) ranked ON ranked.member_id = m.member_id
//        LEFT JOIN image i ON i.image_id = ranked.image_id
//        WHERE m.member_id IN (:memberIds)
//        GROUP BY m.member_id
//    """.trimIndent()
//
//        val query = em.createNativeQuery(sql, "MemberRecommendationMapping") // SQLResultSetMapping 필요
//        query.setParameter("memberIds", memberIds)
//
//        return query.resultList as List<MemberRecommendationResponse>
//
//    }

    override fun findRecommendationMemberByIdIn(memberIds: List<Long>): List<MemberRecommendationResponse> {

        val orderBy = "ORDER BY FIELD(m.member_id, ${memberIds.joinToString(",")})"
        val sql = """
        SELECT
            m.member_id,
            m.nickname,
            m.workout_experience,
            m.workout_style,
            m.workout_goal,
            MAX(CASE WHEN ranked.type = 'PROFILE' THEN i.url END) AS profile_image_url,
            MAX(CASE WHEN ranked.type = 'WORKOUT' THEN i.url END) AS workout_image_url
        FROM member m
        LEFT JOIN (
            SELECT
                mi.member_id,
                mi.type,
                mi.image_id
            FROM (
                SELECT
                    member_id,
                    type,
                    image_id,
                    ROW_NUMBER() OVER (PARTITION BY member_id, type ORDER BY seq ASC) AS rn
                FROM member_image
                WHERE type IN ('PROFILE', 'WORKOUT')
            ) mi
            WHERE mi.rn = 1
        ) ranked ON ranked.member_id = m.member_id
        LEFT JOIN image i ON i.image_id = ranked.image_id
        WHERE m.member_id IN (:memberIds)
        GROUP BY m.member_id
        $orderBy
    """.trimIndent()

        val query = em.createNativeQuery(sql)
        query.setParameter("memberIds", memberIds)


        // Native Query 결과를 Object[]로 받고 DTO로 변환
        return query.resultList.map { row ->
            val arr = row as Array<Any>
            MemberRecommendationResponse(
                memberId = (arr[0] as Number).toLong(),
                nickname = arr[1] as String,
                workoutExperience = (arr[2] as String).let { MemberWorkoutExperience.valueOf(it) },
                workoutStyle = (arr[3] as String).let { MemberWorkoutStyle.valueOf(it) },
                workoutGoal = (arr[4] as String).let { MemberWorkoutGoal.valueOf(it) },
                profileImageUrl = arr[5] as String,
                workoutImageUrl = arr[6] as String?,
            )
        }
    }

    override fun findAllRandomMemberIdByCountAndSeoul(count: Int, seed : Long): List<Long> {
        val sql = """
        SELECT m.member_id
        FROM member m
        JOIN address a ON a.member_id = m.member_id
        WHERE a.si_do = '서울'
        ORDER BY RAND(:seed)
        LIMIT :count
        """.trimIndent()

        val query = em.createNativeQuery(sql)
        query.setParameter("seed", seed)
        query.setParameter("count", count)


        return query.resultList.map { row ->
            row as Long
        }
    }

}