package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import kr.co.fitview.api.app.global.redis.service.RedisService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.cos


@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository : MemberRepository,
    private val workoutPartnerService : WorkoutPartnerService,
    private val addressService : AddressService,
    private val redisService : RedisService,
    private val randomCustom : RandomCustom
) {

    fun findMemberWorkoutRequestProfileFrom(memberId : Long) : MemberWorkoutPartnerProfileResponse? {
        return memberRepository.findMemberWorkoutRequestProfile(memberId)
    }


    fun findMemberFromId(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
    }

    fun findChatMemberFromOrElseThrow(memberId : Long, chatRoomId: Long): ChatMemberProfileResponse {
        val response = memberRepository.findMemberByPrivateChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        return response
    }

    fun findMemberChatProfileFrom(memberId: Long): MemberChatProfileResponse? {
        return memberRepository.findMemberChatProfileByDeletedAtIsNull(memberId)
    }

    fun findMemberReferenceFrom(memberId : Long)  : Member {
        return memberRepository.getReferenceById(memberId)
    }

    fun findMemberProfileFrom(chatRoomIds : List<Long>) : List<ChatRoomMemberProfile>{
        return memberRepository.findAllChatRoomMemberProfile(chatRoomIds)
    }

    fun findMemberProfileFrom(chatRoomId : Long) : List<ChatRoomMemberProfile>{
        return memberRepository.findAllChatRoomMemberProfile(chatRoomId)
    }

    //------------

    fun findMemberFromEmail(email : String) : Member?{
        return memberRepository.findByEmailAndDeletedAtIsNull(email)
    }

    fun findMemberFromProviderId(providerId : String) : Member?{
        return memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)
    }

    fun findMemberOrElseThrow(memberId: Long) : Member {
        return findMemberFromId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    fun findMemberProfile(memberId: Long) : MemberProfileResponse{
        val response = memberRepository.findMemberProfileByDeletedAtIsNull(memberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        return response
    }

    fun findMemberDetail(fromMemberId : Long, toMemberId : Long) : MemberDetailResponse{
        val findProfile = findMemberProfile(toMemberId)
        val otherProfile = OtherMemberProfileResponse.fromMemberProfile(findProfile)

        val findWorkoutPartnerStatus : WorkoutPartnerStatusResponse = workoutPartnerService.findWorkoutPartnerStatus(fromMemberId, toMemberId)
        return MemberDetailResponse(otherProfile, findWorkoutPartnerStatus)
    }

    fun findRandomMemberWithinLocal(
        memberId: Long,
        condition: MemberLocalCondition,
        seed: Long
    ): Page<MemberLocalResponse> {

        val randomMemberId: Long = createRandomMemberId(memberId, seed)

        val findAddress = addressService.findAddressEntityFrom(memberId)
        val boundingBox = createBoundingBox(findAddress!!)

        val findMembers = memberRepository.findMemberWithinLocal(memberId, randomMemberId, boundingBox, condition)
        val shuffledMembers = randomCustom.shuffled(seed, findMembers).toMutableList()

        if(findMembers.size < 100){
            val remainSize = 100 - findMembers.size

            val findMemberIds = findMembers.map{it.memberId}

            val findSeoulMembers = memberRepository.findMemberWithinSeoulByNotMemberIds(
                meMemberId = memberId,
                size = remainSize,
                memberIds = findMemberIds
            )

            val shuffledSeoulMembers = randomCustom.shuffled(seed, findSeoulMembers)

            shuffledMembers.addAll(shuffledSeoulMembers)
        }

        return createProfilePage(condition, shuffledMembers)
    }


    fun findRandomMemberWithinRecommendation(
        memberId: Long,
        size : Int,
        seed: Long = System.currentTimeMillis()
    ) : List<MemberRecommendationResponse>{

        val findMeMember = findMemberOrElseThrow(memberId)

        val findMemberMaxId = memberRepository.findMemberMaxId()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val randomMemberId = randomCustom.nextLong(seed, 1, findMemberMaxId + 1)

        val findMemberByRecommendations = memberRepository.findMemberWithinRecommendation(findMeMember, randomMemberId, size)

        if(findMemberByRecommendations.size == size){
            return randomCustom.shuffled(seed, findMemberByRecommendations)
        }

        val remain = size - findMemberByRecommendations.size
        val memberIds = findMemberByRecommendations.map{it.memberId}

        val findMemberInSeoul = memberRepository.findMemberByNotMemberIdsWithinRecommendationsAndSeoul(
            memberId = memberId,
            memberIds = memberIds,
            size = remain
        )

        return randomCustom.shuffled(seed, findMemberByRecommendations) + randomCustom.shuffled(seed, findMemberInSeoul)
    }

    private fun validateDuplicatedEmail(member: Member) {
        findMemberFromEmail(member.email!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
        }
    }

    private fun validateDuplicatedProviderId(member: Member) {
        findMemberFromProviderId(member.providerId!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_PROVIDER)
        }
    }


    private fun createNewWorkoutTimes(
        workoutTimeNames: List<WorkoutTimeName>,
        member: Member
    ) = workoutTimeNames
        .map { WorkoutTime(member = member, name = it) }

    private fun createBoundingBox(address: Address) : BoundingBox {
        var lat = address.lat
        var lng = address.lng

        if(address.siDo != AddressSiDo.SEOUL){
            lat = AddressConstant.DEFAULT_LAT
            lng = AddressConstant.DEFAULT_LNG
        }

        val latDeg = address.radiusKm?.div(111)
        val latRad = Math.toRadians(address.lat!!)
        val lngDeg = address.radiusKm?.div(111 * cos(latRad))

        val minLat = lat?.minus(latDeg!!)
        val maxLat = lat?.plus(latDeg!!)

        val minLng = lng?.minus(lngDeg!!)
        val maxLng = lng?.plus(lngDeg!!)

        return BoundingBox(
            minLat = minLat!!,
            maxLat = maxLat!!,
            minLng = minLng!!,
            maxLng = maxLng!!
        )
    }

    private fun createProfilePage(
        condition: MemberLocalCondition,
        shuffledMembers: List<MemberLocalResponse>
    ): PageImpl<MemberLocalResponse> {

        val page = (condition.page).coerceAtLeast(1)
        val size = condition.size

        val fromIndex = (page - 1) * size
        val toIndex = (fromIndex + size).coerceAtMost(shuffledMembers.size)

        val pageContent = if (fromIndex >= shuffledMembers.size) emptyList()
        else shuffledMembers.subList(fromIndex, toIndex)

        return PageImpl(pageContent, PageRequest.of(page - 1, size), shuffledMembers.size.toLong())
    }

    private fun createRandomMemberId(memberId: Long, seed: Long): Long {
        val cacheRandomMemberId = redisService.getMemberLocalKey(memberId, seed)
        if (isNotNull(cacheRandomMemberId)) {
            return cacheRandomMemberId!!
        }

        val findMemberMaxId = memberRepository.findMemberMaxId()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val randomMemberId = randomCustom.nextLong(seed, 1, findMemberMaxId + 1)

        redisService.setMemberLocalKey(
            memberId = memberId,
            randomMemberId = randomMemberId,
            seed = seed
        )

        return randomMemberId
    }

    private fun isNotNull(value : Any?) = value != null

}