package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomQueryService
import kr.co.fitview.api.app.domain.favorite.service.FavoriteQueryService
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerQueryService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import kr.co.fitview.api.app.global.redis.service.RedisService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.cos
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository : MemberRepository,
    private val workoutPartnerQueryService : WorkoutPartnerQueryService,
    private val workoutPartnerRequestQueryService : WorkoutPartnerRequestQueryService,
    private val chatRoomQueryService : ChatRoomQueryService,
    private val addressService : AddressService,
    private val redisService : RedisService,
    private val favoriteQueryService : FavoriteQueryService,
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

    fun findAllMemberProfileFrom(chatRoomIds : List<Long>) : List<ChatRoomMemberProfile>{
        return memberRepository.findAllChatRoomMemberProfile(chatRoomIds)
    }

    fun findAllMemberProfileFrom(chatRoomId : Long) : List<ChatRoomMemberProfile>{
        return memberRepository.findAllChatRoomMemberProfile(chatRoomId)
    }

    fun findMemberProfileFrom(memberId : Long, chatRoomId : Long) : ChatRoomMemberProfile? {
        return memberRepository.findChatRoomMemberProfile(memberId, chatRoomId)
    }

    fun findMemberProfileFromMemberId(memberId : Long) : MemberProfile? {
        return memberRepository.findMemberProfileBy(memberId)
    }

    fun findMemberProfileFromMemberId(memberOneId : Long, memberTwoId : Long) : MemberProfiles? {
        return memberRepository.findMemberProfileBy(memberOneId, memberTwoId)
    }

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

        val findWorkoutPartnerStatus : WorkoutPartnerStatusResponse = workoutPartnerQueryService.findWorkoutPartnerStatus(fromMemberId, toMemberId)

        val isFavorite = favoriteQueryService.findFavoriteFrom(
            fromMemberId = fromMemberId,
            toMemberId = toMemberId
        ) != null

        return MemberDetailResponse(
            profile = otherProfile,
            workoutPartner = findWorkoutPartnerStatus,
            isFavorite = isFavorite
        )
    }

    fun findRandomMemberWithinLocal(
        memberId: Long,
        condition: MemberLocalCondition,
        seed: Long
    ): Page<MemberLocalResponse> {

        val randomMemberId: Long = createRandomMemberId(memberId, seed)

        val findAddress = addressService.findAddressEntityFrom(memberId)
        val boundingBox = createBoundingBox(findAddress!!, condition.radiusKm)

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

        val memberPage = createProfilePage(condition, shuffledMembers)
        val otherMemberIds = memberPage.content.map { it.memberId }

        val findWorkoutPartnerRequests = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = memberId,
            otherMemberIds = otherMemberIds
        )

        val findChatRooms = chatRoomQueryService.findChatRoomFrom(memberId, otherMemberIds)

        val requestMap = findWorkoutPartnerRequests.associateBy { request ->
            if (request.getFromMemberId() == memberId) request.getToMemberId() else request.getFromMemberId()
        }

        val chatRoomMap = findChatRooms.associate {
            it.otherMemberId to it.chatRoomId
        }

        return memberPage.map { member ->

            val request = requestMap[member.memberId]

            val lastWorkoutPartnerRequest = request?.let { req ->

                val chatRoomId = if (req.status == WorkoutPartnerRequestStatus.ACCEPT) {
                    chatRoomMap[member.memberId]
                } else {
                    null
                }

                LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = req.id!!,
                    status = req.status!!,
//                    isSentByMe = req.getFromMemberId() == memberId,
                    chatRoomId = chatRoomId
                )
            }

            member.copy(lastWorkoutPartnerRequest = lastWorkoutPartnerRequest)
        }
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

        val response : List<MemberRecommendationResponse>

        if(findMemberByRecommendations.size >= size){
            response = randomCustom.shuffled(seed, findMemberByRecommendations)
        }
        else{
            val remain = size - findMemberByRecommendations.size
            val memberIds = findMemberByRecommendations.map{it.memberId}

            val findMemberInSeoul = memberRepository.findMemberByNotMemberIdsWithinRecommendationsAndSeoul(
                memberId = memberId,
                memberIds = memberIds,
                size = remain
            )

            response = randomCustom.shuffled(seed, findMemberByRecommendations) + randomCustom.shuffled(seed, findMemberInSeoul)
        }

        val otherMemberIds = response.map { it.memberId }

        val findWorkoutPartnerRequests = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = memberId,
            otherMemberIds = otherMemberIds
        )

        val findChatRooms = chatRoomQueryService.findChatRoomFrom(memberId, otherMemberIds)

        val requestMap = findWorkoutPartnerRequests.associateBy { request ->
            if (request.getFromMemberId() == memberId) request.getToMemberId() else request.getFromMemberId()
        }

        val chatRoomMap = findChatRooms.associate {
            it.otherMemberId to it.chatRoomId
        }

        return response.map { member ->

            val request = requestMap[member.memberId]

            val lastWorkoutPartnerRequest = request?.let { req ->

                val chatRoomId = if (req.status == WorkoutPartnerRequestStatus.ACCEPT) {
                    chatRoomMap[member.memberId]
                } else {
                    null
                }

                LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = req.id!!,
                    status = req.status!!,
//                    isSentByMe = req.getFromMemberId() == memberId,
                    chatRoomId = chatRoomId
                )
            }

            member.copy(lastWorkoutPartnerRequest = lastWorkoutPartnerRequest)
        }

    }

    fun findOtherMemberFrom(memberId : Long, chatRoomId : Long) : Member? {
        return memberRepository.findOtherMemberBy(memberId, chatRoomId)
    }

    fun findAllMemberFrom(condition: AdminMemberCondition) : Slice<AdminMemberResponse> {
       return memberRepository.findAllMemberBy(condition)
    }

    fun findDeletedMemberFrom(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNotNull(memberId)
    }

    fun countMemberFromCreatedAtDate(date : LocalDate) : Int{
        return memberRepository.countMemberByCreatedAtDate(date)
    }

    fun countNotSignupMemberFromCreatedAtDate(date : LocalDate) : Int{
        return memberRepository.countNotSignupMemberByCreatedAtDate(date)
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

    private fun createBoundingBox(address: Address, radiusKm : Int) : BoundingBox {
        var lat = address.lat
        var lng = address.lng

        if(address.siDo != AddressSiDo.SEOUL){
            lat = AddressConstant.DEFAULT_LAT
            lng = AddressConstant.DEFAULT_LNG
        }

        val latDeg = radiusKm.toDouble().div(111)
        val latRad = Math.toRadians(address.lat!!)
        val lngDeg = radiusKm.toDouble().div(111 * cos(latRad))

        val minLat = lat?.minus(latDeg)
        val maxLat = lat?.plus(latDeg)

        val minLng = lng?.minus(lngDeg)
        val maxLng = lng?.plus(lngDeg)

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