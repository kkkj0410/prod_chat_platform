package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.image.dto.request.WorkoutImageUrlServiceRequest
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.entity.MemberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.image.ImageErrorCode
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ImageServiceTest @Autowired constructor(
    val imageService: ImageService,
    val imageRepository: ImageRepository,
    val memberImageRepository : MemberImageRepository,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service
) : IntegrationTestSupport(){

    @DisplayName("회원 프로필 이미지를 저장한다.")
    @Test
    fun saveMemberImageProfile() {
        // given
        val imageUrl = "url"
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMemberImage = imageService.saveMemberImageProfile(
            member = savedMember,
            profileImageUrl = imageUrl
        )

        // then
        assertThat(findMemberImage)
            .extracting("member", "type")
            .contains(savedMember, MemberImageType.PROFILE)
        assertThat(findMemberImage.image!!.url).isEqualTo(imageUrl)
    }

    @DisplayName("회원 프로필 이미지가 존재하면 덮어씌워서 새 프로필을 저장한다.")
    @Test
    fun saveMemberImageProfileExistingProfile() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val image = Image(
            url = "url"
        )
        val savedImage = imageRepository.save(image)

        val memberImage = MemberImage(
            savedMember,
            savedImage,
            MemberImageType.PROFILE,
        )
        memberImageRepository.save(memberImage)

        val imageUrl = "imageUrl"

        // when
        val findMemberImage = imageService.saveMemberImageProfile(
            member = savedMember,
            profileImageUrl = imageUrl
        )

        // then
        assertThat(findMemberImage)
            .extracting("member", "type")
            .contains(savedMember, MemberImageType.PROFILE)
        assertThat(findMemberImage.image!!.url).isEqualTo(imageUrl)
    }


    @DisplayName("회원 운동 사진을 저장한다.")
    @Test
    fun saveMemberImageWorkout() {
        // given
        val imageUrl1 = "imageUrl1"
        val imageUrl2 = "imageUrl2"

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val requests = listOf(
            imageUrl1,
            imageUrl2,
        )

        // when
        val findMemberImages = imageService.saveMemberImageWorkouts(
            member = savedMember,
            imageUrls = requests
        )

        // then
        assertThat(findMemberImages)
            .extracting("member", "type", "seq", "image.url")
            .containsExactlyInAnyOrder(
                tuple(savedMember, MemberImageType.WORKOUT, 100, imageUrl1),
                tuple(savedMember, MemberImageType.WORKOUT, 200, imageUrl2),
            )
    }

    @DisplayName("회원 운동 사진을 저장 시, 운동 사진이 비었으면 기존 운동 사진을 삭제하고 끝낸다.")
    @Test
    fun saveMemberImageWorkoutEmptyList() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf("one", "two")
        )
        oAuth2Service.signup(request, member.id!!)

        val requests = listOf<String>()

        // when
        imageService.saveMemberImageWorkouts(
            member = member,
            imageUrls = requests
        )

        // then
        val findImages = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(member.id!!)
        assertThat(findImages).hasSize(0)
    }


    @DisplayName("회원 운동 사진 개수가 3개를 초과하면 저장하지 않는다.")
    @Test
    fun saveMemberImageWorkoutExceedSize() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val requests = listOf(
            "imageUrl1",
            "imageUrl2",
            "imageUrl3",
            "imageUrl4",
        )

        assertThatThrownBy {
            imageService.saveMemberImageWorkouts(
                member = savedMember,
                imageUrls = requests
            )
        }
        .isInstanceOf(GlobalException::class.java)
        .satisfies(ThrowingConsumer { ex ->
            val globalEx = ex as GlobalException
            assertThat(globalEx.errorCode)
                .isEqualTo(ImageErrorCode.MEMBER_WORKOUT_IMAGE_LIMIT)
        })

    }

    @DisplayName("회원 운동 사진이 기존에 존재할 경우, 기존 사진을 전부 삭제하고 요청 사진을 추가한다.")
    @Test
    fun saveMemberImageWorkoutExistingImage() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val image1 = Image(
            url = "url1"
        )
        val image2 = Image(
            url = "url2"
        )
        val savedImage1 = imageRepository.save(image1)
        val savedImage2 = imageRepository.save(image2)

        val memberImage1 = MemberImage(
            savedMember,
            savedImage1,
            MemberImageType.WORKOUT,
        )
        val memberImage2 = MemberImage(
            savedMember,
            savedImage2,
            MemberImageType.WORKOUT,
        )
        memberImageRepository.save(memberImage1)
        memberImageRepository.save(memberImage2)

        val requests = listOf(
            "imageUrl1",
            "imageUrl2",
            "imageUrl3",
        )

        // when
        val responseMemberImages = imageService.saveMemberImageWorkouts(
            member = savedMember,
            imageUrls = requests
        )

        // then
        assertThat(responseMemberImages)
            .extracting("member", "type", "seq", "image.url")
            .containsExactlyInAnyOrder(
                tuple(savedMember, MemberImageType.WORKOUT, 100, "imageUrl1"),
                tuple(savedMember, MemberImageType.WORKOUT, 200, "imageUrl2"),
                tuple(savedMember, MemberImageType.WORKOUT, 300, "imageUrl3"),
                )

        val findMemberImages = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(savedMember.id!!)
        assertThat(findMemberImages).hasSize(3)
        assertThat(findMemberImages)
            .extracting("member", "type", "seq", "image.url")
            .containsExactlyInAnyOrder(
                tuple(savedMember, MemberImageType.WORKOUT, 100, "imageUrl1"),
                tuple(savedMember, MemberImageType.WORKOUT, 200, "imageUrl2"),
                tuple(savedMember, MemberImageType.WORKOUT, 300, "imageUrl3"),
            )
    }
}