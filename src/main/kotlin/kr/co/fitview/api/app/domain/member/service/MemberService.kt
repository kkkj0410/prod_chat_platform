package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutDay
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutDayName
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutDayRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository : MemberRepository,
    private val workoutDayRepository: WorkoutDayRepository,
    private val workoutTimeRepository: WorkoutTimeRepository
) {

    @Transactional
    fun addMember(member : Member) : Member{
        validateDuplicatedEmail(member)

        return memberRepository.save(member)
    }

    @Transactional
    fun addWorkoutDays(member : Member, workoutDayNames : List<WorkoutDayName>) : List<WorkoutDay>{

        val findWorkoutDays = workoutDayRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        val findWorkoutDayNameSet = createWorkoutDayNameSet(findWorkoutDays)

        val newWorkoutDays = createNewWorkoutDays(workoutDayNames, findWorkoutDayNameSet, member)

        workoutDayRepository.saveAll(newWorkoutDays)

        return findWorkoutDays + newWorkoutDays
    }

    @Transactional
    fun addWorkoutTimes(member : Member, workoutTimeNames : List<WorkoutTimeName>) : List<WorkoutTime>{

        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        val findWorkoutTimeNameSet = createWorkoutTimeNameSet(findWorkoutTimes)

        val newWorkoutTimes = createNewWorkoutTimes(workoutTimeNames, findWorkoutTimeNameSet, member)

        workoutTimeRepository.saveAll(newWorkoutTimes)

        return findWorkoutTimes + newWorkoutTimes
    }

    fun findMemberFromLoginId(loginId : String) : Member?{
        return memberRepository.findByEmailAndDeletedAtIsNull(loginId)
    }

    fun findMemberFromProviderId(providerId : String) : Member?{
        return memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)
    }


    fun findMemberFromLoginId(memberId : Long) : Member?{
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
    }

    fun findMemberOrElseThrow(memberId: Long) : Member {
        return findMemberFromLoginId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    fun findMemberMe(memberId: Long): MemberMeResponse {
        val findMember = findMemberOrElseThrow(memberId)
        return MemberMeResponse(findMember.email!!, findMember.role!!)
    }

    private fun validateDuplicatedEmail(member: Member) {
        findMemberFromLoginId(member.email!!)?.let {
            throw GlobalException(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
        }
    }

    private fun createWorkoutDayNameSet(workoutDays: List<WorkoutDay>): Set<WorkoutDayName> =
        workoutDays.map { it.name!! }.toSet()

    private fun createNewWorkoutDays(
        workoutDayNames: List<WorkoutDayName>,
        findWorkoutNameSet: Set<WorkoutDayName>,
        member: Member
    ) = workoutDayNames
        .filterNot { it in findWorkoutNameSet }
        .map { WorkoutDay(member = member, name = it) }

    private fun createWorkoutTimeNameSet(workoutTimes: List<WorkoutTime>) : Set<WorkoutTimeName> =
        workoutTimes.map { it.name!! }.toSet()

    private fun createNewWorkoutTimes(
        workoutTimeNames: List<WorkoutTimeName>,
        findWorkoutNameSet: Set<WorkoutTimeName>,
        member: Member
    ) = workoutTimeNames
        .filterNot { it in findWorkoutNameSet }
        .map { WorkoutTime(member = member, name = it) }



}