package kr.co.fitview.api.app.domain.member.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.image.entity.MemberImage
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.global.entity.BaseEntity
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDate

@Entity
@Table(name = "member")
class Member(

    @Column(name = "email", nullable = false, length = 100)
    var email: String? = null,

    @Column(name = "password", nullable = false)
    var password: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: Role? = Role.USER,

    @Lob
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    var provider: OAuth2Provider? = null,

    @Column(name = "provider_id", length = 100)
    var providerId: String? = null,

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_signup", nullable = false)
    var isSignup: Boolean? = false,

    @Size(max = 100)
    @Column(name = "nickname", length = 100)
    var nickname: String? = null,

    @Size(max = 1000)
    @Column(name = "intro", length = 1000)
    var intro: String? = null,

    @Size(max = 100)
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 100)
    var gender: Gender? = null,

    @Column(name = "birthday")
    var birthday: LocalDate? = null,

    @Column(name = "height")
    var height: Int? = null,

    @Column(name = "weight")
    var weight: Int? = null,

    @Size(max = 100)
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_goal", length = 100)
    var workoutGoal: MemberWorkoutGoal? = null,

    @Size(max = 100)
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_style", length = 100)
    var workoutStyle: MemberWorkoutStyle? = null,

    @Size(max = 100)
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_experience", length = 100)
    var workoutExperience: MemberWorkoutExperience? = null

    ) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    var id: Long? = null

    @OneToMany(mappedBy = "member")
    var memberImages: MutableSet<MemberImage> = mutableSetOf()

    @OneToMany(mappedBy = "member")
    var workoutTimes: MutableSet<WorkoutTime> = mutableSetOf()

    @OneToMany(mappedBy = "member")
    var addresses: MutableSet<Address> = mutableSetOf()
}