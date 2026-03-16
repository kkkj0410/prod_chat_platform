package kr.co.fitview.api.app.domain.favorite.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.entity.Favorite
import kr.co.fitview.api.app.domain.favorite.repository.FavoriteRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FavoriteQueryServiceTest @Autowired constructor(
    val favoriteQueryService: FavoriteQueryService,
    val favoriteRepository : FavoriteRepository,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
    val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val time: Time
) : IntegrationTestSupport() {

    @DisplayName("찜한 회원들을 조회한다.")
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
            nickname = "nick1",
            workoutImageUrls = null
        )
        val other2SignupData = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick2",
            workoutImageUrls = listOf("hello")
        )
        val other3SignupData = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick3",
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
        )

        // when
        val response = favoriteQueryService.findFavoriteMembers(
            memberId = me.id!!,
            condition = condition
        )

        // then
        assertThat(response.content).hasSize(3)
        assertThat(response.hasNext()).isFalse()

        assertThat(response.content)
            .extracting("memberId", "workoutImageUrl")
            .containsExactly(
                tuple(other3.id, "hello2"),
                tuple(other2.id, "hello"),
                tuple(other1.id,  null)
            )

        assertThat(response.content)
            .extracting("favoriteId")
            .containsExactly(
                favorite3.id,
                favorite2.id,
                favorite1.id
            )
    }

    @DisplayName("찜한 회원들을 조회 시, 운동 파트너 요청 여부도 조회한다.")
    @Test
    fun findFavoriteMembersLastWorkoutPartnerRequest() {
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
            nickname = "nick1",
            workoutImageUrls = null
        )
        val other2SignupData = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick2",
            workoutImageUrls = listOf("hello")
        )
        val other3SignupData = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick3",
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

        val workoutPartnerRequest1 = WorkoutPartnerRequest(
            fromMember = me,
            toMember = other1,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest2 = WorkoutPartnerRequest(
            fromMember = other2,
            toMember = me,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest3 = WorkoutPartnerRequest(
            fromMember = me,
            toMember = other3,
            status = WorkoutPartnerRequestStatus.ACCEPT,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )

        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = me,
            memberTwo = other3,
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(type = ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatParticipant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = me,
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = other3,
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val condition = FavoriteMemberCondition(
        )

        // when
        val response = favoriteQueryService.findFavoriteMembers(
            memberId = me.id!!,
            condition = condition
        )

        // then
        assertThat(response.content).hasSize(3)
        assertThat(response.hasNext()).isFalse()

        assertThat(response.content)
            .extracting(
                "memberId",
                "lastWorkoutPartnerRequest.workoutPartnerRequestId",
                "lastWorkoutPartnerRequest.status",
                "lastWorkoutPartnerRequest.chatRoomId"
            )
            .containsExactly(
                tuple(
                    other3.id,
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status!!,
                    chatRoom.id!!
                ),

                tuple(
                    other2.id,
                    workoutPartnerRequest2.id!!,
                    workoutPartnerRequest2.status!!,
                    null
                ),

                tuple(
                    other1.id,
                    workoutPartnerRequest1.id!!,
                    workoutPartnerRequest1.status!!,
                    null
                )
            )
    }

    @DisplayName("상대 회원을 찜했는지 조회한다.")
    @Test
    fun findFavoriteFrom() {
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
        val findFavorite = favoriteQueryService.findFavoriteFrom(
            fromMemberId = me.id!!,
            toMemberId = other.id!!
        )

        // then
        assertThat(findFavorite!!.id).isNotNull()

        assertThat(findFavorite)
            .extracting("fromMember", "toMember")
            .contains(me, other)
    }
}