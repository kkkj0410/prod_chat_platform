package kr.co.fitview.api.app.global.util

import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.global.entity.Gender
import java.time.LocalDate

object TestDataFactory {

    fun oAuth2SignupRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName> = listOf(
            WorkoutTimeName.WEEKDAY_DAWN,
            WorkoutTimeName.WEEKDAY_EVENING
        ),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String> = listOf("imageUrl1", "imageUrl2"),
        intro: String = "intro",
        address: AddressCreateServiceRequest = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
    ): OAuth2SignupServiceRequest {
        return OAuth2SignupServiceRequest(
            profileImageUrl = profileImageUrl,
            nickname = nickname,
            gender = gender,
            birthday = LocalDate.of(2000, 1, 1),
            height = 170,
            weight = 65,
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutTimes = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            workoutImageUrls = listOf("imageUrl1", "imageUrl2"),
            intro = "intro",
            address = AddressCreateServiceRequest(
                siDo = AddressSiDo.SEOUL,
                siGunGu = "강남구",
                eupMyeonDong = "역삼동",
                lat = 37.4979,
                lng = 127.0276,
                fullAddress = "서울특별시 강남구 테헤란로 123"
            )
        )
    }
}