package kr.co.fitview.api.app.domain.image.repository

import com.querydsl.core.NonUniqueResultException
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.entity.MemberImage
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberImageRepositoryTest @Autowired constructor(
    val memberImageRepository: MemberImageRepository,
    val imageRepository: ImageRepository,
    val memberRepository: MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("회원 프로필 이미지를 조회한다.")
    @Test
    fun findWithImageByMemberIdAndProfileAndDeletedAtIsNull() {
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

        // when
        val findMemberImage = memberImageRepository.findWithImageByMemberIdAndProfileAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findMemberImage)
            .extracting("member", "image", "type")
            .contains(savedMember, savedImage, MemberImageType.PROFILE)

    }

    @DisplayName("회원 운동 사진을 조회한다.")
    @Test
    fun findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull() {
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

        // when
        val findMemberImages = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findMemberImages)
            .extracting("member", "image", "type")
            .containsExactlyInAnyOrder(
                tuple(savedMember, savedImage1, MemberImageType.WORKOUT),
                tuple(savedMember, savedImage2, MemberImageType.WORKOUT),
            )

    }



}