package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Gender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MemberProfileResponseTest : IntegrationTestSupport(){


    @DisplayName("1개 회원의 중복된 프로필 데이터에서 중복을 제거한다.")
    @Test
    fun fromFlat() {
        // given
        val baseFlat = MemberProfileFlat(
            memberId = 1L,
            nickname = "nickname",
            gender = Gender.MALE,
            addressId = 123L,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            intro = "운동은 인생.",
            height = 178,
            weight = 78,
            birthday = LocalDate.of(1995, 6, 10),
            workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            workoutTimeName = WorkoutTimeName.WEEKDAY_EVENING,
            score = 4.8,
            imageUrl = "",
            memberImageType = MemberImageType.PROFILE
        )

        val memberFlats = listOf(
            baseFlat.copy(
                imageUrl = "https://static.fitview.com/images/members/1_profile.png",
                memberImageType = MemberImageType.PROFILE,
                workoutTimeName = WorkoutTimeName.WEEKDAY_EVENING
            ),
            baseFlat.copy(
                imageUrl = "https://static.fitview.com/images/members/1_workout_1.png",
                memberImageType = MemberImageType.WORKOUT,
                workoutTimeName = WorkoutTimeName.WEEKDAY_MORNING
            ),
            baseFlat.copy(
                imageUrl = "https://static.fitview.com/images/members/1_workout_2.png",
                memberImageType = MemberImageType.WORKOUT,
                workoutTimeName = WorkoutTimeName.WEEKEND_DAWN
            )
        )
        // when
        val response = MemberProfileResponse.fromFlat(memberFlats)

        // then
        assertThat(response!!.memberId).isEqualTo(baseFlat.memberId)
        assertThat(response.nickname).isEqualTo(baseFlat.nickname)
        assertThat(response.gender).isEqualTo(baseFlat.gender)
        assertThat(response.siDo).isEqualTo(baseFlat.siDo)
        assertThat(response.siGunGu).isEqualTo(baseFlat.siGunGu)
        assertThat(response.eupMyeonDong).isEqualTo(baseFlat.eupMyeonDong)
        assertThat(response.intro).isEqualTo(baseFlat.intro)
        assertThat(response.height).isEqualTo(baseFlat.height)
        assertThat(response.weight).isEqualTo(baseFlat.weight)
        assertThat(response.workoutExperience).isEqualTo(baseFlat.workoutExperience)
        assertThat(response.workoutStyle).isEqualTo(baseFlat.workoutStyle)
        assertThat(response.workoutGoal).isEqualTo(baseFlat.workoutGoal)

        assertThat(response.profileImageUrl)
            .isEqualTo("https://static.fitview.com/images/members/1_profile.png")

        assertThat(response.workoutTimeNames)
            .containsExactlyInAnyOrder(
                WorkoutTimeName.WEEKDAY_EVENING,
                WorkoutTimeName.WEEKDAY_MORNING,
                WorkoutTimeName.WEEKEND_DAWN
            )

        assertThat(response.workoutImageUrls)
            .containsExactlyInAnyOrder(
                "https://static.fitview.com/images/members/1_workout_1.png",
                "https://static.fitview.com/images/members/1_workout_2.png"
            )
    }
}