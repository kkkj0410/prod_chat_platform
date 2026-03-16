package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.member.dto.request.MemberReserveNicknameServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.response.MemberReserveNicknameResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.redis.service.RedisService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository : MemberRepository,
    private val memberQueryService : MemberQueryService,
    private val workoutTimeRepository: WorkoutTimeRepository,
    private val imageService : ImageService,
    private val redisService : RedisService,
    private val time : Time,
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

        findWorkoutTimes.forEach{
            it.delete(time.nowLocalDateTime)
        }

        val newWorkoutTimes = createNewWorkoutTimes(workoutTimeNames, member)

        workoutTimeRepository.saveAll(newWorkoutTimes)

        return newWorkoutTimes
    }

    @Transactional
    fun removeMember(memberId : Long) : Member{
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        return findMember.delete(time.nowLocalDateTime)
    }

    @Transactional
    fun modifyMember(memberId: Long, request: MemberUpdateServiceRequest) : Member{
        val findMember = findMemberOrElseThrow(memberId)

        if(isNotNull(request.nickname)){
            findMember.nickname = request.nickname
        }

        if(isNotNull(request.intro)){
            findMember.intro = request.intro
        }

        if(isNotNull(request.height)){
            findMember.height = request.height
        }

        if(isNotNull(request.weight)){
            findMember.weight = request.weight
        }

        if(isNotNull(request.birthday)){
            findMember.birthday = request.birthday
        }

        if(isNotNull(request.workoutExperience)){
            findMember.workoutExperience = request.workoutExperience
        }

        if(isNotNull(request.workoutStyle)){
            findMember.workoutStyle = request.workoutStyle
        }

        if(isNotNull(request.workoutGoal)){
            findMember.workoutGoal = request.workoutGoal
        }

        if(isNotNull(request.workoutTimes)){
            this.addWorkoutTimes(
                member = findMember,
                workoutTimeNames = request.workoutTimes!!
            )
        }

        if(isNotNull(request.profileImageUrl)){
            imageService.saveMemberImageProfile(
                member = findMember,
                profileImageUrl = request.profileImageUrl!!
            )
        }

        if(isNotNull(request.workoutImageUrls)){
            imageService.saveMemberImageWorkouts(
                member = findMember,
                imageUrls = request.workoutImageUrls!!
            )
        }

        return findMember
    }


    fun reserveNickname(memberId: Long, request: MemberReserveNicknameServiceRequest): MemberReserveNicknameResponse {
        val findMember = memberQueryService.findMemberFromId(memberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
        if(findMember.isSignup!!) return MemberReserveNicknameResponse(isReserved = false)

        val isExistsNickname = memberQueryService.existsMemberNickname(request.nickname)
        if (isExistsNickname) return MemberReserveNicknameResponse(isReserved = false)

        val currentReservedMemberId = getReservedMemberId(request.nickname)

        return when {
            currentReservedMemberId == memberId.toString() -> {
                renewNicknameReserve(memberId, request.nickname)
                MemberReserveNicknameResponse(isReserved = true)
            }
            currentReservedMemberId != null -> {
                MemberReserveNicknameResponse(isReserved = false)
            }
            else -> {
                MemberReserveNicknameResponse(isReserved = reserveNicknameKeys(memberId, request.nickname))
            }
        }
    }

    private fun getReservedMemberId(nickname: String): String? =
        redisService.getKey(getNicknameKey(nickname))

    private fun renewNicknameReserve(memberId: Long, nickname: String) {
        redisService.setKey(getNicknameKey(nickname), memberId.toString(), RESERVE_NICKNAME_TTL)
        redisService.setKey(getReverseNicknameMemberKey(memberId), nickname, RESERVE_NICKNAME_TTL)
    }

    private fun reserveNicknameKeys(memberId: Long, nickname: String): Boolean {
        val previousNickname = redisService.getKey(getReverseNicknameMemberKey(memberId))
        if (previousNickname != null) {
            redisService.deleteKey(getNicknameKey(previousNickname))
            redisService.deleteKey(getReverseNicknameMemberKey(memberId))
        }

        val isSuccess = redisService.setIfAbsentKey(getNicknameKey(nickname), memberId.toString(), RESERVE_NICKNAME_TTL)
        if (isSuccess) {
            redisService.setKey(getReverseNicknameMemberKey(memberId), nickname, RESERVE_NICKNAME_TTL)
        }
        return isSuccess
    }

    companion object {
        private const val RESERVE_NICKNAME_TTL = 30L
        private fun getNicknameKey(nickname: String) = "member:nickname:reserve:$nickname"
        private fun getReverseNicknameMemberKey(memberId: Long) = "member:nickname:reserve:member:$memberId"
    }

    private fun findMemberOrElseThrow(memberId: Long) : Member {
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    private fun validateDuplicatedEmail(member: Member) {
        memberRepository.findByEmailAndDeletedAtIsNull(member.email!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
        }
    }

    private fun validateDuplicatedProviderId(member: Member) {
        memberRepository.findByProviderIdAndDeletedAtIsNull(member.providerId!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_PROVIDER)
        }
    }

    private fun createNewWorkoutTimes(
        workoutTimeNames: List<WorkoutTimeName>,
        member: Member
    ) = workoutTimeNames
        .map { WorkoutTime(member = member, name = it) }


    private fun isNotNull(value : Any?) = value != null


}