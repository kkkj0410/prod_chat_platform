package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.QMemberLocalResponse
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle


class MemberRepositoryImpl(
    private val queryFactory : JPAQueryFactory
) : MemberRepositoryCustom {

    override fun findMemberWithinLocal(memberId: Long, condition: MemberLocalCondition) : List<MemberLocalResponse>{

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
            .join(member.memberImages, memberImage)
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


                address.lat.between(
                    JPAExpressions.select(address.lat.subtract(address.radiusKm.divide(111)))
                        .from(address)
                        .where(address.member.id.eq(30L)),
                    JPAExpressions.select(address.lat.add(address.radiusKm.divide(111)))
                        .from(address)
                        .where(address.member.id.eq(30L)),

                )
                    .and(
                        address.lng.between(
                            JPAExpressions.select(
                                address.lng.subtract(
                                    address.radiusKm.divide(111).divide(
                                        Expressions.numberTemplate(
                                            Double::class.java, "COS(RADIANS({0}))", address.lat
                                        )
                                    )
                                )
                            ).from(address).where(address.member.id.eq(memberId)),
                            JPAExpressions.select(
                                address.lng.add(
                                    address.radiusKm.divide(111).divide(
                                        Expressions.numberTemplate(
                                            Double::class.java, "COS(RADIANS({0}))", address.lat
                                        )
                                    )
                                )
                            ).from(address).where(address.member.id.eq(memberId))
                        )
                    )
                    .and(
                        memberImage.type.eq(MemberImageType.PROFILE)
                    )
            )
            .limit(100)
            .fetch()

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


    private fun betweenAge(ages : List<Age>?) : BooleanExpression?{
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

    private fun inWorkoutGoal(workoutGoals : List<MemberWorkoutGoal>?) : BooleanExpression?{
        if (workoutGoals.isNullOrEmpty()) return null
        return member.workoutGoal.`in`(workoutGoals)
    }

    private fun inWorkoutStyle(workoutStyles : List<MemberWorkoutStyle>?) : BooleanExpression?{
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