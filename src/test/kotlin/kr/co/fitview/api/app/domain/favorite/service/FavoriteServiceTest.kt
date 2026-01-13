package kr.co.fitview.api.app.domain.favorite.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.favorite.repository.FavoriteRepository
import kr.co.fitview.api.app.domain.favorite.repository.FavoriteRepositoryTest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FavoriteServiceTest @Autowired constructor(
    val favoriteService: FavoriteService,
    val favoriteRepository : FavoriteRepository,
    val memberRepository : MemberRepository,
    val time: Time
) : IntegrationTestSupport() {

    @DisplayName("회원을 찜한다.")
    @Test
    fun favoriteAdd() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        // when
        val savedFavorite = favoriteService.favoriteAdd(
            memberId = member1.id!!,
            targetMemberId = member2.id!!
        )

        // then
        assertThat(savedFavorite)
            .extracting("fromMember", "toMember")
            .contains(member1, member2)
    }

    @DisplayName("회원을 찜할 시, 이미 찜이 되어있다면 기존 찜을 반환한다.")
    @Test
    fun favoriteAddAlreadyFavorite() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val favorite = Favorite(
            fromMember = member1,
            toMember = member2
        )
        favoriteRepository.save(favorite)

        // when
        val savedFavorite = favoriteService.favoriteAdd(
            memberId = member1.id!!,
            targetMemberId = member2.id!!
        )

        // then
        assertThat(savedFavorite.id!!).isEqualTo(favorite.id!!)
    }

    @DisplayName("회원 찜을 삭제한다.")
    @Test
    fun favoriteDelete() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val favorite = Favorite(
            fromMember = member1,
            toMember = member2
        )
        favoriteRepository.save(favorite)

        // when
        favoriteService.favoriteDelete(
            memberId = member1.id!!,
            targetMemberId = member2.id!!
        )

        // then
        val findFavorites = favoriteRepository.findAll()
        assertThat(findFavorites).isEmpty()
    }

    @DisplayName("회원 찜 삭제 시, 찜이 없다면 삭제하지 않는다.")
    @Test
    fun favoriteDeleteNotExistsFavorite() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        // when
        favoriteService.favoriteDelete(
            memberId = member1.id!!,
            targetMemberId = member2.id!!
        )

        // then
        val findFavorites = favoriteRepository.findAll()
        assertThat(findFavorites).isEmpty()
    }
}