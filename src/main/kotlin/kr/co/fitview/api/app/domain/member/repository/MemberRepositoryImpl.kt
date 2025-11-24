package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.JPAExpressions.select
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.address.entity.QAddress
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.QWorkoutTime.workoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle


class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em: EntityManager
) : MemberRepositoryCustom {


    override fun findMemberWithinLocal(memberId: Long, condition: MemberLocalCondition): List<MemberLocalResponse> {

//        SELECT a.*
//                from member m
//        join address a
//        on m.member_id = a.member_id
//                WHERE a.lat BETWEEN
//        (SELECT lat - radius_km/111 FROM address WHERE member_id = 30)
//        AND (SELECT lat + radius_km/111 FROM address WHERE member_id = 30)
//        AND a.lng BETWEEN
//        (SELECT lng - radius_km/(111 * COS(RADIANS(lat))) FROM address WHERE member_id = 30)
//        AND (SELECT lng + radius_km/(111 * COS(RADIANS(lat))) FROM address WHERE member_id = 30);

        val defaultLat = 37.4900861966502
        val defaultLng = 127.01953478052

        val subAddress1 = QAddress("subAddress1")
        val subAddress2 = QAddress("subAddress2")
        val subAddress3 = QAddress("subAddress3")
        val subAddress4 = QAddress("subAddress4")

        val subLat1 = CaseBuilder()
            .`when`(subAddress1.siDo.eq(AddressSiDo.SEOUL))
            .then(subAddress1.lat)
            .otherwise(defaultLat)

        val subLat2 = CaseBuilder()
            .`when`(subAddress2.siDo.eq(AddressSiDo.SEOUL))
            .then(subAddress2.lat)
            .otherwise(defaultLat)

        val subLng3 = CaseBuilder()
            .`when`(subAddress3.siDo.eq(AddressSiDo.SEOUL))
            .then(subAddress3.lng)
            .otherwise(defaultLng)

        val subLng4 = CaseBuilder()
            .`when`(subAddress4.siDo.eq(AddressSiDo.SEOUL))
            .then(subAddress4.lng)
            .otherwise(defaultLng)

        return queryFactory
            .select(
                QMemberLocalResponse(
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    image.url
                )
            )
            .from(member)
            .join(address).on(member.id.eq(address.member.id))
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                gteHeight(condition.minHeight),
                lteHeight(condition.maxHeight),
                gteWeight(condition.minWeight),
                lteWeight(condition.maxWeight),
                betweenAge(condition.age),
                inWorkoutGoal(condition.workoutGoal),
                inWorkoutStyle(condition.workoutStyle),
                betweenWorkoutExperience(condition.minWorkoutExperience, condition.maxWorkoutExperience),
                member.id.ne(memberId),
                address.siDo.eq(AddressSiDo.SEOUL),

                address.lat.between(
                    JPAExpressions.select(subLat1.subtract(subAddress1.radiusKm.divide(111)))
                        .from(subAddress1)
                        .where(subAddress1.member.id.eq(memberId)),
                    JPAExpressions.select(subLat2.add(subAddress2.radiusKm.divide(111)))
                        .from(subAddress2)
                        .where(subAddress2.member.id.eq(memberId)),

                    )
                    .and(
                        address.lng.between(
                            JPAExpressions.select(
                                subLng3.subtract(
                                    subAddress3.radiusKm.divide(111).divide(
                                        Expressions.numberTemplate(
                                            Double::class.java, "COS(RADIANS({0}))", subAddress3.lat
                                        )
                                    )
                                )
                            ).from(subAddress3).where(subAddress3.member.id.eq(memberId)),
                            JPAExpressions.select(
                                subLng4.add(
                                    subAddress4.radiusKm.divide(111).divide(
                                        Expressions.numberTemplate(
                                            Double::class.java, "COS(RADIANS({0}))", subAddress4.lat
                                        )
                                    )
                                )
                            ).from(subAddress4).where(subAddress4.member.id.eq(memberId))
                        )
                    )
                    .and(
                        memberImage.type.eq(MemberImageType.PROFILE)
                    )
            )
            .limit(100)
            .fetch()

    }

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

    override fun findAllRandomMemberIdByCountAndSeoul(count: Int, seed: Long): List<Long> {
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

    override fun findMemberProfileByDeletedAtIsNull(memberId: Long): MemberProfileResponse? {

        val memberProfileFlats = queryFactory
            .select(
                QMemberProfileFlat(
                    member.id,
                    member.nickname,
                    member.gender,
                    address.siDo,
                    address.siGunGu,
                    address.eupMyeonDong,
                    member.intro,
                    member.height,
                    member.weight,
                    member.birthday,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    workoutTime.name,
                    member.score,
                    image.url,
                    memberImage.type
                )
            )
            .from(member)
            .join(member.mutableWorkoutTimes, workoutTime)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .join(member.mutableAddresses, address)
            .where(
                member.id.eq(memberId),
                member.deletedAt.isNull,
                workoutTime.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
                address.deletedAt.isNull
            )
            .fetch()



        return MemberProfileResponse.fromFlat(memberProfileFlats)
    }

    override fun findMemberChatProfileByDeletedAtIsNull(memberId: Long): MemberChatProfileResponse? {
        return queryFactory
            .select(
                Projections.constructor(
                    MemberChatProfileResponse::class.java,
                    image.url,
                    member.nickname,
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                member.id.eq(memberId),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
                memberImage.type.eq(MemberImageType.PROFILE)
            )
            .fetchOne()
    }

    override fun findMemberWorkoutRequestProfile(memberId: Long): MemberWorkoutPartnerProfileResponse? {

        return queryFactory
            .select(
                Projections.constructor(
                    MemberWorkoutPartnerProfileResponse::class.java,
                    member.id,
                    image.url,
                    member.nickname
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .limit(1)
            .fetchOne()
    }

    private fun gteHeight(minHeight: Int?): BooleanExpression? {
        return minHeight?.let { member.height.goe(it) }
    }

    private fun lteHeight(maxHeight: Int?): BooleanExpression? {
        return maxHeight?.let { member.height.loe(it) }
    }

    private fun gteWeight(minWeight: Int?): BooleanExpression? {
        return minWeight?.let { member.weight.goe(it) }
    }

    private fun lteWeight(maxWeight: Int?): BooleanExpression? {
        return maxWeight?.let { member.weight.loe(it) }
    }


    private fun betweenAge(ages: List<Age>?): BooleanExpression? {
        if (ages.isNullOrEmpty()) return null

        val koreanAge = Expressions.numberTemplate(
            Int::class.java,
            "YEAR(current_date) - YEAR({0}) + 1",
            member.birthday
        )

        var predicate: BooleanExpression? = null

        for (age in ages) {
            val ageCondition = koreanAge.between(
                Expressions.constant(age.min),
                Expressions.constant(age.max)
            )
            predicate = predicate?.or(ageCondition) ?: ageCondition
        }

        return predicate
    }

    private fun inWorkoutGoal(workoutGoals: List<MemberWorkoutGoal>?): BooleanExpression? {
        if (workoutGoals.isNullOrEmpty()) return null
        return member.workoutGoal.`in`(workoutGoals)
    }

    private fun inWorkoutStyle(workoutStyles: List<MemberWorkoutStyle>?): BooleanExpression? {
        if (workoutStyles.isNullOrEmpty()) return null
        return member.workoutStyle.`in`(workoutStyles)
    }

    private fun betweenWorkoutExperience(
        minWorkoutExperience: MemberWorkoutExperience?,
        maxWorkoutExperience: MemberWorkoutExperience?
    ): BooleanExpression? {
        if (minWorkoutExperience == null && maxWorkoutExperience == null) {
            return null
        }

        val all = MemberWorkoutExperience.entries

        val targetExperiences = when {
            minWorkoutExperience != null && maxWorkoutExperience != null ->
                all.filter { it.ordinal in minWorkoutExperience.ordinal..maxWorkoutExperience.ordinal }

            minWorkoutExperience != null ->
                all.filter { it.ordinal >= minWorkoutExperience.ordinal }

            maxWorkoutExperience != null ->
                all.filter { it.ordinal <= maxWorkoutExperience.ordinal }

            else -> emptyList()
        }

        return member.workoutExperience.`in`(targetExperiences)

    }
}