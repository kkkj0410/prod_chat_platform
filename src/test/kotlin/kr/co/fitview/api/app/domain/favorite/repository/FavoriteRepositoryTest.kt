package kr.co.fitview.api.app.domain.favorite.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.favorite.entity.QFavorite.favorite
import kr.co.fitview.api.app.domain.favorite.service.FavoriteService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FavoriteRepositoryTest @Autowired constructor(
    val favoriteRepository : FavoriteRepository,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
    val time: Time
) : IntegrationTestSupport() {

    @DisplayName("상대 회원에 대한 찜 여부를 조회한다.")
    @Test
    fun findByFromMemberIdAndToMemberId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val favorite = Favorite(
            fromMember = me,
            toMember = other
        )
        favoriteRepository.save(favorite)

        // when
        val findFavorite = favoriteRepository.findByFromMemberIdAndToMemberId(
            fromMemberId = me.id!!,
            toMemberId = other.id!!
        )

        // then
        assertThat(findFavorite!!.id).isNotNull()

        assertThat(findFavorite)
            .extracting("fromMember", "toMember")
            .contains(me, other)
    }

    @DisplayName("찜 대상 회원들을 조회한다.")
    @Test
    fun findFavoriteMembers() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)
        memberRepository.save(other2)
        memberRepository.save(other3)

        val other1SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = null
        )
        val other2SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf("hello")
        )
        val other3SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf("hello2")
        )
        oAuth2Service.signup(other1SignupData, other1.id!!)
        oAuth2Service.signup(other2SignupData, other2.id!!)
        oAuth2Service.signup(other3SignupData, other3.id!!)


        val favorite1 = Favorite(
            fromMember = me,
            toMember = other1
        )
        val favorite2 = Favorite(
            fromMember = me,
            toMember = other2
        )
        val favorite3 = Favorite(
            fromMember = me,
            toMember = other3
        )
        favoriteRepository.save(favorite1)
        favoriteRepository.save(favorite2)
        favoriteRepository.save(favorite3)

        val condition = FavoriteMemberCondition()

        // when
        val response = favoriteRepository.findFavoriteMembers(
            memberId = me.id!!,
            condition = condition
        )

        // then
        assertThat(response.content).hasSize(3)
        assertThat(response.hasNext()).isFalse()

        assertThat(response.content)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(other3.id, other3.nickname, "hello2"),
                tuple(other2.id, other2.nickname, "hello"),
                tuple(other1.id, other1.nickname, null)
            )

        assertThat(response.content)
            .extracting("favoriteId")
            .containsExactly(
                favorite3.id,
                favorite2.id,
                favorite1.id
            )
    }

    @DisplayName("찜 대상 회원 조회 시, 삭제된 계정은 조회하지 않는다.")
    @Test
    fun findFavoriteMembersNotDeletedMember() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)

        val other1SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = null
        )
        oAuth2Service.signup(other1SignupData, other1.id!!)

        val favorite1 = Favorite(
            fromMember = me,
            toMember = other1
        )
        favoriteRepository.save(favorite1)

        other1.delete(time.nowLocalDateTime)

        val condition = FavoriteMemberCondition()

        // when
        val response = favoriteRepository.findFavoriteMembers(
            memberId = me.id!!,
            condition = condition
        )

        // then
        assertThat(response.content).hasSize(0)
    }


    @DisplayName("찜 대상 회원들을 조회 시, favoriteId 지정값 보다 더 낮은 대상 찜만 조회한다")
    @Test
    fun findFavoriteMembersExistsCursor() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)
        memberRepository.save(other2)
        memberRepository.save(other3)

        val other1SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = null
        )
        val other2SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf("hello")
        )
        val other3SignupData = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf("hello2")
        )
        oAuth2Service.signup(other1SignupData, other1.id!!)
        oAuth2Service.signup(other2SignupData, other2.id!!)
        oAuth2Service.signup(other3SignupData, other3.id!!)


        val favorite1 = Favorite(
            fromMember = me,
            toMember = other1
        )
        val favorite2 = Favorite(
            fromMember = me,
            toMember = other2
        )
        val favorite3 = Favorite(
            fromMember = me,
            toMember = other3
        )
        favoriteRepository.save(favorite1)
        favoriteRepository.save(favorite2)
        favoriteRepository.save(favorite3)

        val condition = FavoriteMemberCondition(
            cursorFavoriteId = favorite3.id!!
        )

        // when
        val response = favoriteRepository.findFavoriteMembers(
            memberId = me.id!!,
            condition = condition
        )

        // then
        assertThat(response.content).hasSize(2)
        assertThat(response.hasNext()).isFalse()

        assertThat(response.content)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(other2.id, other2.nickname, "hello"),
                tuple(other1.id, other1.nickname, null)
            )

        assertThat(response.content)
            .extracting("favoriteId")
            .containsExactly(
                favorite2.id,
                favorite1.id
            )
    }
}