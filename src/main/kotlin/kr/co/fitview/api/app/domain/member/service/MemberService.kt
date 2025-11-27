package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.service.AddressService
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
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.cos
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository : MemberRepository,
    private val workoutTimeRepository: WorkoutTimeRepository,
    private val workoutPartnerService : WorkoutPartnerService,
    private val addressService : AddressService,
    private val time : Time
) {

    @Transactional
    fun addMember(member : Member) : Member{
        validateDuplicatedEmail(member)

        return memberRepository.save(member)
    }

    @Transactional
    fun addMemberByOAuth2(member : Member) : Member{
        validateDuplicatedProviderId(member)

        return memberRepository.save(member)
    }

    @Transactional
    fun addWorkoutTimes(member : Member, workoutTimeNames : List<WorkoutTimeName>) : List<WorkoutTime>{

        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        val findWorkoutTimeNameSet = createWorkoutTimeNameSet(findWorkoutTimes)

        val newWorkoutTimes = createNewWorkoutTimes(workoutTimeNames, findWorkoutTimeNameSet, member)

        workoutTimeRepository.saveAll(newWorkoutTimes)

        return findWorkoutTimes + newWorkoutTimes
    }

    @Transactional
    fun removeMember(memberId : Long) : Member{
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        return findMember.delete(time.nowLocalDateTime)
    }

    fun findMemberFromEmail(email : String) : Member?{
        return memberRepository.findByEmailAndDeletedAtIsNull(email)
    }

    fun findMemberFromProviderId(providerId : String) : Member?{
        return memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)
    }

    fun findMemberFromId(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
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


    fun findMemberReferenceFrom(memberId : Long)  : Member{
        return memberRepository.getReferenceById(memberId)
    }

    fun findMemberChatProfileFrom(memberId: Long): MemberChatProfileResponse? {
        return memberRepository.findMemberChatProfileByDeletedAtIsNull(memberId)
    }

    fun findRandomMemberWithinLocal(
        memberId: Long,
        condition: MemberLocalCondition,
        seed: Long
    ): Page<MemberLocalResponse> {
        val randomId = 123L

        val findAddress = addressService.findAddressEntityFrom(memberId)

        val boundingBox = createBoundingBox(findAddress!!)

        val findMembers = memberRepository.findMemberWithinLocal(memberId, randomId, boundingBox, condition)

        val random = Random(seed)
        val shuffledMembers = findMembers.shuffled(random)

        val page = (condition.page ?: 1).coerceAtLeast(1)
        val size = condition.size ?: 10

        val fromIndex = (page - 1) * size
        val toIndex = (fromIndex + size).coerceAtMost(shuffledMembers.size)

        val pageContent = if (fromIndex >= shuffledMembers.size) emptyList()
        else shuffledMembers.subList(fromIndex, toIndex)

        return PageImpl(pageContent, PageRequest.of(page - 1, size), shuffledMembers.size.toLong())
    }

    fun findRandomMemberWithinRecommendation(memberId: Long) : List<MemberRecommendationResponse>{
        val seed: Long = System.currentTimeMillis()

        val findMemberIds = memberRepository.findAllMemberIdWithinRecommendation(memberId)

        if(findMemberIds.size >= 10){
            val random = Random(seed)
            val shuffledMemberIds = findMemberIds.shuffled(random).take(10)
            return memberRepository.findRecommendationMemberByIdIn(shuffledMemberIds)

        }
        else{
            val findCount = 10 - findMemberIds.size

            val findRandomMemberIds = memberRepository.findAllRandomMemberIdByCountAndSeoul(findCount, seed)

            return memberRepository.findRecommendationMemberByIdIn(findMemberIds + findRandomMemberIds)
        }
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

    private fun createWorkoutTimeNameSet(workoutTimes: List<WorkoutTime>) : Set<WorkoutTimeName> =
        workoutTimes.map { it.name!! }.toSet()

    private fun createNewWorkoutTimes(
        workoutTimeNames: List<WorkoutTimeName>,
        findWorkoutNameSet: Set<WorkoutTimeName>,
        member: Member
    ) = workoutTimeNames
        .filterNot { it in findWorkoutNameSet }
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
}