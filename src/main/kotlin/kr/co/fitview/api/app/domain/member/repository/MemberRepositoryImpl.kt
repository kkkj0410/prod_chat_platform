package kr.co.fitview.api.app.domain.member.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.address.entity.QAddress.address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.response.OtherMemberProfileResponse
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant.chatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.image.entity.QImage
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.member.entity.QWorkoutTime.workoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.LocalDate


class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em: EntityManager,
    private val time: Time
) : MemberRepositoryCustom {


    override fun findMemberWithinLocal(
        memberId: Long,
        randomMemberId: Long,
        boundingBox: BoundingBox,
        condition: MemberLocalCondition
    ): List<MemberLocalResponse> {

        val MAX_FETCH = 100

        val left = findMemberWithinLocalByCondition(
            randomMemberId,
            memberId,
            boundingBox,
            condition,
            isFromRandom = true,
            size = MAX_FETCH
        )

        val remain = MAX_FETCH - left.size

        val right =
            if (remain > 0)
                findMemberWithinLocalByCondition(
                    randomMemberId,
                    memberId,
                    boundingBox,
                    condition,
                    isFromRandom = false,
                    size = remain
                )
            else
                emptyList()

        val combined = left + right

        return combined.take(MAX_FETCH)
    }


    override fun findMemberWithinSeoulByNotMemberIds(
        meMemberId: Long,
        size: Int,
        memberIds: List<Long>
    ): List<MemberLocalResponse> {
        return queryFactory
            .select(
                Projections.constructor(
                    MemberLocalResponse::class.java,
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .join(member.mutableAddresses, address)
            .where(
                member.deletedAt.isNull,
                member.id.ne(meMemberId),
                member.id.notIn(memberIds),
                address.siDo.eq(AddressSiDo.SEOUL),
                memberImage.type.eq(MemberImageType.PROFILE),
                memberImage.deletedAt.isNull,
            )
            .limit(size.toLong())
            .fetch()
    }


    private fun findMemberWithinLocalByCondition(
        randomMemberId: Long,
        memberId: Long,
        boundingBox: BoundingBox,
        condition: MemberLocalCondition,
        isFromRandom: Boolean,
        size: Int
    ): List<MemberLocalResponse> {

        val results = queryFactory
            .select(
                Projections.constructor(
                    MemberLocalResponse::class.java,
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .join(member.mutableAddresses, address)
            .where(
                if (isFromRandom) {
                    member.id.goe(randomMemberId)
                } else {
                    member.id.goe(1).and(member.id.lt(randomMemberId))
                },
                member.deletedAt.isNull,
                member.id.ne(memberId),
                address.siDo.eq(AddressSiDo.SEOUL),
                address.lat.between(boundingBox.minLat, boundingBox.maxLat),
                address.lng.between(boundingBox.minLng, boundingBox.maxLng),
                memberImage.type.eq(MemberImageType.PROFILE),
                memberImage.deletedAt.isNull,
                gteHeight(condition.minHeight),
                lteHeight(condition.maxHeight),
                gteWeight(condition.minWeight),
                lteWeight(condition.maxWeight),
                betweenAge(condition.age),
                inWorkoutGoal(condition.workoutGoal),
                inWorkoutStyle(condition.workoutStyle),
                betweenWorkoutExperience(condition.minWorkoutExperience, condition.maxWorkoutExperience),
            )
            .limit(size.toLong())
            .fetch()

        return results
    }

    override fun findMemberWithinRecommendation(member: Member, randomMemberId : Long, size: Int): List<MemberRecommendationResponse> {

        val first = findMemberByRandomMemberIdWithinRecommendation(member, randomMemberId, size, true)
        val second = findFirstSearchMemberByRandomMemberIdWithinRecommendation(size, first, member, randomMemberId)

        val combined = first + second

        return addWorkoutImageUrl(combined)
    }


    override fun findMemberByNotMemberIdsWithinRecommendationsAndSeoul(memberId: Long, memberIds : List<Long>,size: Int): List<MemberRecommendationResponse> {

        val findMember = queryFactory
            .select(
                Projections.constructor(
                    MemberRecommendationResponse::class.java,
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .join(member.mutableAddresses, address)
            .where(
                address.siDo.eq(AddressSiDo.SEOUL),
                member.id.ne(memberId),
                member.id.notIn(memberIds),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
                address.deletedAt.isNull
            )
            .limit(size.toLong())
            .fetch()

        return addWorkoutImageUrl(findMember)
    }


    override fun findMemberProfileByDeletedAtIsNull(memberId: Long): MemberProfileResponse? {

        val memberProfileFlats = queryFactory
            .select(
                QMemberProfileFlat(
                    member.id,
                    member.nickname,
                    member.gender,
                    address.id,
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
                member.id.eq(memberId),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .limit(1)
            .fetchOne()
    }

    override fun findMemberMaxId(): Long? {
        return queryFactory
            .select(member.id.max())
            .from(member)
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
            "{0} - YEAR({1}) + 1",
            time.nowLocalDate.year,
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

    private fun findMemberByRandomMemberIdWithinRecommendation(
        member: Member,
        randomMemberId : Long,
        size: Int,
        isFromRandom : Boolean
    ): List<MemberRecommendationResponse> {

        val targetMember = QMember("targetMember")

        val matchCount: NumberExpression<Int> =
            CaseBuilder()
                .`when`(targetMember.workoutExperience.eq(member.workoutExperience)).then(1).otherwise(0)
                .add(CaseBuilder().`when`(targetMember.workoutStyle.eq(member.workoutStyle)).then(1).otherwise(0))
                .add(CaseBuilder().`when`(targetMember.workoutGoal.eq(member.workoutGoal)).then(1).otherwise(0))

        val findMember = queryFactory
            .select(
                Projections.constructor(
                    MemberRecommendationResponse::class.java,
                    targetMember.id,
                    targetMember.nickname,
                    targetMember.workoutExperience,
                    targetMember.workoutStyle,
                    targetMember.workoutGoal,
                    image.url
                )
            )
            .from(targetMember)
            .join(targetMember.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .join(targetMember.mutableAddresses , address)
            .where(
                if (isFromRandom) {
                    targetMember.id.goe(randomMemberId)
                } else {
                    targetMember.id.goe(1).and(targetMember.id.lt(randomMemberId))
                },
                targetMember.id.ne(member.id),
                memberImage.type.eq(MemberImageType.PROFILE),
                matchCount.goe(2),
                address.siDo.eq(AddressSiDo.SEOUL),
                targetMember.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
                address.deletedAt.isNull
            )
            .limit(size.toLong())
            .fetch()
        return findMember
    }

    private fun findFirstSearchMemberByRandomMemberIdWithinRecommendation(
        size: Int,
        first: List<MemberRecommendationResponse>,
        member: Member,
        randomMemberId: Long
    ): List<MemberRecommendationResponse> {
        val remain = size - first.size
        val second =
            if (remain > 0)
                findMemberByRandomMemberIdWithinRecommendation(
                    member,
                    randomMemberId,
                    size,
                    false
                )
            else
                emptyList()
        return second
    }


    private fun addWorkoutImageUrl(
        members: List<MemberRecommendationResponse>,
    ): List<MemberRecommendationResponse> {
        val memberIds = members.map { it.memberId }

        val findMemberAllWorkoutImages = queryFactory
            .select(
                Projections.constructor(
                    MemberImageResponse::class.java,
                    member.id,
                    image.url,
                    memberImage.seq
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                member.id.`in`(memberIds),
                memberImage.type.eq(MemberImageType.WORKOUT),
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull
            )
            .fetch()

        return members.map { target ->
            target.copy(
                workoutImageUrl = findMemberAllWorkoutImages
                    .filter { it.memberId == target.memberId }
                    .minByOrNull { it.seq }
                    ?.imageUrl
            )
        }
    }


    override fun findMemberByPrivateChatRoomId(memberId : Long, chatRoomId: Long): ChatMemberProfileResponse? {

        val meChatParticipant = QChatParticipant("meChatParticipant")
        val otherChatParticipant = QChatParticipant("otherChatParticipant")

        val meMember = QMember("meMember")
        val otherMember = QMember("otherMember")

        val meMemberImage = QMemberImage("meMemberImage")
        val otherMemberImage = QMemberImage("otherMemberImage")

        val meImage = QImage("meImage")
        val otherImage = QImage("otherImage")

        val findMembers = queryFactory
            .select(
                Projections.constructor(
                    ChatMemberProfileResponseFlat::class.java,
                    meMember.id,
                    meMember.nickname,
                    meImage.url,
                    otherMember.id,
                    otherMember.nickname,
                    otherImage.url
                )
            )
            .from(chatRoom)
            .join(meChatParticipant)
                .on(
                    meChatParticipant.chatRoom.id.eq(chatRoomId),
                    meChatParticipant.member.id.eq(memberId)
                )
            .join(otherChatParticipant)
                .on(
                    otherChatParticipant.chatRoom.id.eq(chatRoomId),
                    otherChatParticipant.member.id.ne(memberId)
                )
            .join(meMember)
                .on(
                    meMember.id.eq(meChatParticipant.member.id)
                )
            .join(otherMember)
                .on(
                    otherMember.id.eq(otherChatParticipant.member.id)
                )
            .join(meMemberImage)
                .on(
                    meMemberImage.member.id.eq(meMember.id),
                    meMemberImage.type.eq(MemberImageType.PROFILE)
                )
            .join(otherMemberImage)
                .on(
                    otherMemberImage.member.id.eq(otherMember.id),
                    otherMemberImage.type.eq(MemberImageType.PROFILE)
                )
            .join(meImage)
                .on(
                    meImage.id.eq(meMemberImage.image.id)
                )
            .join(otherImage)
                .on(
                    otherImage.id.eq(otherMemberImage.image.id)
                )
            .where(
                chatRoom.id.eq(chatRoomId),
                chatRoom.type.eq(ChatRoomType.PRIVATE),
                chatRoom.deletedAt.isNull
            )
            .fetchOne()

        return findMembers?.toResponse()
    }

    override fun findAllChatRoomMemberProfile(chatRoomIds : List<Long>) : List<ChatRoomMemberProfile>{

        return queryFactory
            .select(
                Projections.constructor(
                    ChatRoomMemberProfile::class.java,
                    chatRoom.id,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(chatRoom)
            .join(chatRoom.chatParticipants, chatParticipant)
            .join(chatParticipant.member, member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                chatRoom.id.`in`(chatRoomIds),
                memberImage.type.eq(MemberImageType.PROFILE),
            )
            .fetch()
    }

    override fun findAllChatRoomMemberProfile(chatRoomId: Long): List<ChatRoomMemberProfile> {
        return queryFactory
            .select(
                Projections.constructor(
                    ChatRoomMemberProfile::class.java,
                    chatRoom.id,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(chatRoom)
            .join(chatRoom.chatParticipants, chatParticipant)
            .join(chatParticipant.member, member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                chatRoom.id.eq(chatRoomId),
                memberImage.type.eq(MemberImageType.PROFILE),
            )
            .fetch()
    }

    override fun findChatRoomMemberProfile(memberId: Long, chatRoomId: Long): ChatRoomMemberProfile? {
        return queryFactory
            .select(
                Projections.constructor(
                    ChatRoomMemberProfile::class.java,
                    chatRoom.id,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(chatRoom)
            .join(chatRoom.chatParticipants, chatParticipant)
            .join(chatParticipant.member, member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                member.id.eq(memberId),
                chatRoom.id.eq(chatRoomId),
                memberImage.type.eq(MemberImageType.PROFILE),
            )
            .fetchOne()
    }

    override fun findOtherMemberBy(memberId: Long, chatRoomId: Long): Member? {
        return queryFactory
            .select(member)
            .from(chatParticipant)
            .join(chatRoom)
            .on(
                chatParticipant.chatRoom.id.eq(chatRoom.id),
            )
            .join(chatParticipant.member, member)
            .where(
                chatParticipant.member.id.ne(memberId),
                chatRoom.deletedAt.isNull,
                chatRoom.id.eq(chatRoomId),
                chatRoom.type.eq(ChatRoomType.PRIVATE)
            )
            .fetchOne()
    }

    override fun findMemberProfileBy(memberId: Long): MemberProfile? {
        return queryFactory
            .select(
                Projections.constructor(
                    MemberProfile::class.java,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                member.id.eq(memberId),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull
            )
            .fetchOne()
    }

    override fun findMemberProfileBy(memberOneId: Long, memberTwoId: Long): MemberProfiles {

        val profiles = queryFactory
            .select(
                Projections.constructor(
                    MemberProfile::class.java,
                    member.id,
                    member.nickname,
                    image.url
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                member.id.`in`(memberOneId, memberTwoId),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                memberImage.deletedAt.isNull
            )
            .fetch()

        val profileMap = profiles.associateBy { it.memberId }

        return MemberProfiles(
            memberOne = profileMap[memberOneId],
            memberTwo = profileMap[memberTwoId]
        )
    }

    override fun findAllMemberBy(condition: AdminMemberCondition): Slice<AdminMemberResponse> {

        val size = condition.size
        val memberId = condition.memberId

        val results = queryFactory
            .select(
                Projections.constructor(
                    AdminMemberResponse::class.java,
                    member.id,
                    member.email,
                    member.provider,
                    member.nickname,
                    member.gender,
                    member.birthday,
                    member.height,
                    member.weight,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal,
                    address.fullAddress,
                )
            )
            .from(member)
            .join(member.mutableAddresses, address)
            .where(
                member.isSignup.isTrue,
                member.role.eq(Role.USER),
                member.provider.isNotNull,
                address.deletedAt.isNull,
                memberId?.let { member.id.lt(it) }
            )
            .orderBy(member.id.desc())
            .limit(size.toLong() + 1)
            .fetch()

        val hasNext = results.size > size
        val content = if (hasNext) results.dropLast(1) else results

        val memberIds = content.map { it.memberId }

        if (memberIds.isEmpty()) {
            return SliceImpl(content, PageRequest.of(0, size), hasNext)
        }

        val reviewCountMap: Map<Long, Long> =
            queryFactory
                .select(review.toMember.id, review.count())
                .from(review)
                .where(
                    review.toMember.id.`in`(memberIds),
                    review.deletedAt.isNull
                )
                .groupBy(review.toMember.id)
                .fetch()
                .associate { it.get(0, Long::class.java)!! to it.get(1, Long::class.java)!! }


        val memberIdsWithImage: Set<Long> =
            queryFactory
                .select(memberImage.member.id)
                .from(memberImage)
                .where(
                    memberImage.member.id.`in`(memberIds),
                    memberImage.type.eq(MemberImageType.WORKOUT),
                    memberImage.image.id.isNotNull,
                    memberImage.deletedAt.isNull,
                )
                .fetch()
                .toSet()

        // 4️⃣ 결과 조립
        val finalContent = content.map {
            it.copy(
                reviewCount = reviewCountMap[it.memberId] ?: 0L,
                hasWorkoutImageUrl = memberIdsWithImage.contains(it.memberId)
            )
        }

        return SliceImpl(finalContent, PageRequest.of(0, size), hasNext)
    }

    override fun countMemberByCreatedAtDate(date: LocalDate): Int {
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return queryFactory
            .select(member.count())
            .from(member)
            .where(
                member.createdAt.goe(start),
                member.createdAt.lt(end),
                member.role.eq(Role.USER)
            )
            .fetchOne()?.toInt() ?: 0
    }

    override fun countNotSignupMemberByCreatedAtDate(date: LocalDate): Int {
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        return queryFactory
            .select(member.count())
            .from(member)
            .where(
                member.createdAt.goe(start),
                member.createdAt.lt(end),
                member.role.eq(Role.USER),
                member.isSignup.eq(false),
            )
            .fetchOne()?.toInt() ?: 0
    }
}