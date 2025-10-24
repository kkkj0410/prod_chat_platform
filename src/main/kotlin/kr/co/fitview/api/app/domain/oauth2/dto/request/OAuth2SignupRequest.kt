package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.enums.*
import kr.co.fitview.api.app.global.entity.Gender
import java.time.LocalDate

data class OAuth2SignupRequest(

    @field:NotNull(message = "profileImageUrl is required")
    val profileImageUrl : String?,

    @field:NotNull(message = "nickname is required")
    val nickname : String?,

    @field:NotNull(message = "gender is required")
    val gender : Gender?,

    @field:NotNull(message = "birthday is required")
    val birthday : LocalDate?,

    @field:NotNull(message = "height is required")
    val height : Int?,

    @field:NotNull(message = "weight is required")
    val weight : Int?,

    @field:NotNull(message = "workoutExperience is required")
    val workoutExperience : MemberWorkoutExperience?,

    @field:NotNull(message = "workoutStyle is required")
    val workoutStyle : MemberWorkoutStyle?,

    @field:NotNull(message = "workoutTimes is required")
    @field:Size(min = 1, message = "workoutTimes cannot be empty")
    val workoutTimes : List<WorkoutTimeName>?,

    @field:NotNull(message = "workoutGoal is required")
    val workoutGoal : MemberWorkoutGoal?,

    @field:Size(min = 1, message = "workoutImageUrls cannot be empty")
    val workoutImageUrls : List<String>?,

    val intro : String?,

    @field:NotNull(message = "terms is required")
    val terms : List<TermRequest>?

    ){
    fun toServiceRequest() : OAuth2SignupServiceRequest{
        return OAuth2SignupServiceRequest(
            profileImageUrl = this.profileImageUrl!!,
            nickname = this.nickname!!,
            gender = this.gender!!,
            birthday = this.birthday!!,
            height = this.height!!,
            weight = this.weight!!,
            workoutExperience = this.workoutExperience!!,
            workoutStyle = this.workoutStyle!!,
            workoutTimes = this.workoutTimes!!,
            workoutGoal = this.workoutGoal!!,
            workoutImageUrls = this.workoutImageUrls,
            intro = this.intro,
            terms = this.terms!!
        )
    }
}
