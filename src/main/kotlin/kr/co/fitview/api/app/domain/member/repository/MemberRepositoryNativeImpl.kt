package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
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
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.QMemberLocalResponse
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

class MemberRepositoryNativeImpl(
    private val queryFactory : JPAQueryFactory,
    private val em: EntityManager
) : MemberRepositoryNative {

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