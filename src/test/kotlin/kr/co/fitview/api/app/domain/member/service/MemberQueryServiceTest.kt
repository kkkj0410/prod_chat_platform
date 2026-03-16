package kr.co.fitview.api.app.domain.member.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class MemberQueryServiceTest @Autowired constructor(
    val memberQueryService: MemberQueryService,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val em : EntityManager,
    val addressRepository: AddressRepository,
    val randomCustom : RandomCustom,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너 요청에서 요청자의 프로필을 조회한다.")
    @Test
    fun findMemberWorkoutRequestProfileFrom() {
        // given
        val me = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val findMemberProfile = memberQueryService.findMemberWorkoutRequestProfileFrom(me.id!!)

        // then
        assertThat(findMemberProfile)
            .extracting("memberId", "profileImageUrl", "nickname")
            .contains(me.id!!, request.profileImageUrl, request.nickname)
    }


    @DisplayName("채팅방의 각 회원을 조회한다.")
    @Test
    fun findChatMemberFromOrElseThrow() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)


        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick2",
            profileImageUrl = "updateProfile2"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        // when
        val response = memberQueryService.findChatMemberFromOrElseThrow(me.id!!, chatRoom.id!!)

        // then
        assertThat(response.me)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl)

        assertThat(response.other)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl)
    }

    @DisplayName("채팅방의 각 회원 조회가 안되면 에러를 발생시킨다.")
    @Test
    fun findChatMemberFromOrElseThrowNotFoundMember() {
        // when & then
        assertThatThrownBy {
            memberQueryService.findChatMemberFromOrElseThrow(123L, 123L)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }

    @DisplayName("회원의 채팅방 프로필을 확인한다.")
    @Test
    fun findMemberChatProfileFrom() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val response = memberQueryService.findMemberChatProfileFrom(me.id!!)

        // then
        assertThat(response)
            .extracting("profileImageUrl", "nickname")
            .contains(request.profileImageUrl, request.nickname)
    }

    @DisplayName("채팅방 회원 프로필 조회")
    @Test
    fun findAllMemberProfileFrom() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick2",
            profileImageUrl = "updateProfile2"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)


        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        val chatRoomIds = listOf(chatRoom.id!!, chatRoom2.id!!)

        // when
        val findMembers = memberQueryService.findAllMemberProfileFrom(chatRoomIds)

        // then
        assertThat(findMembers).hasSize(4)
        assertThat(findMembers)
            .extracting("chatRoomId", "memberId", "nickname", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom.id!!, other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl),
                tuple(chatRoom2.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom2.id!!, other2.id!!, signupRequest3.nickname, signupRequest3.profileImageUrl),
            )
    }

    @DisplayName("특정 채팅방 회원 프로필 조회")
    @Test
    fun findAllMemberProfileFromByChatRoomId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)


        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        // when
        val findMembers = memberQueryService.findAllMemberProfileFrom(chatRoom.id!!)

        // then
        assertThat(findMembers).hasSize(2)
        assertThat(findMembers)
            .extracting("chatRoomId", "memberId", "nickname", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom.id!!, other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl),
            )
    }

    @DisplayName("회원을 프록시로 조회한다.")
    @Test
    fun findMemberReferenceFrom() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        em.flush()
        em.clear()

        // when
        val memberProxy = memberQueryService.findMemberReferenceFrom(savedMember.id!!)

        // then
        assertThat(memberProxy).isInstanceOf(HibernateProxy::class.java)
        assertThat(memberProxy::class.simpleName!!).contains("Member")
        assertThat(memberProxy)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, member.role)
    }

    @DisplayName("로그인 정보로 회원을 찾는다.")
    @Test
    fun findMemberFromId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberQueryService.findMemberFromEmail(member.email!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)

    }

    @DisplayName("존재하지 않는 회원이면 로그인 정보로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromWithoutMemberLoginId() {
        // given
        val email = "email"

        // when
        val findMember = memberQueryService.findMemberFromEmail(email)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("회원 id로 회원을 찾는다.")
    @Test
    fun findMemberFromMemberIdLoginId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberQueryService.findMemberFromId(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)

    }

    @DisplayName("존재하지 않는 회원이면 회원 id로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromMemberIdWithoutMemberLoginId() {
        // given
        val memberId = 100L

        // when
        val findMember = memberQueryService.findMemberFromId(memberId)

        // then
        assertThat(findMember).isNull()
    }

    fun createOAuth2SignupServiceRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String> = listOf(
            "imageUrl1",
            "imageUrl2",
        ),
        intro: String = "intro",
        address : AddressCreateServiceRequest = AddressCreateServiceRequest(
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
            birthday = birthday,
            height = height,
            weight = weight,
            workoutExperience = workoutExperience,
            workoutStyle = workoutStyle,
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            address = address
        )
    }


    @DisplayName("회원 id로 회원 정보를 조회한다.")
    @Test
    fun findMemberProfile() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, member.id!!)

        // when
        val response = memberQueryService.findMemberProfile(savedMember.id!!)

        // then
        assertThat(response).isNotNull
        response.let {
            assertThat(it.memberId).isEqualTo(savedMember.id)
            assertThat(it.nickname).isEqualTo(request.nickname)
            assertThat(it.gender).isEqualTo(request.gender)
            assertThat(it.height).isEqualTo(request.height)
            assertThat(it.weight).isEqualTo(request.weight)
            assertThat(it.intro).isEqualTo(request.intro)
            assertThat(it.age).isEqualTo(Age.fromBirthDay(request.birthday))
            assertThat(it.workoutExperience).isEqualTo(request.workoutExperience)
            assertThat(it.workoutStyle).isEqualTo(request.workoutStyle)
            assertThat(it.workoutGoal).isEqualTo(request.workoutGoal)
            assertThat(it.score).isEqualTo(savedMember.score!!.toInt())

            assertThat(it.siDo).isEqualTo(request.address.siDo)
            assertThat(it.siGunGu).isEqualTo(request.address.siGunGu)
            assertThat(it.eupMyeonDong).isEqualTo(request.address.eupMyeonDong)

            assertThat(it.workoutTimeNames).containsExactlyInAnyOrderElementsOf(request.workoutTimes)
            assertThat(it.workoutImageUrls).containsExactlyInAnyOrderElementsOf(request.workoutImageUrls)

            assertThat(it.profileImageUrl).isEqualTo(request.profileImageUrl)
        }
    }

    @DisplayName("회원 정보가 없으면 회원 고유 id로 사용자 정보를 조회할 수 없다.")
    @Test
    fun findMemberProfileWithoutMember() {
        // given
        val memberId = 100L

        // when & then
        assertThatThrownBy {
            memberQueryService.findMemberProfile(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(GlobalErrorCode.ENTITY_NOT_FOUND)
            })
    }


    @DisplayName("회원이 존재하면 조회한다.")
    @Test
    fun findMemberOrElseThrow() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberQueryService.findMemberOrElseThrow(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)
    }

    @DisplayName("회원이 존재하지 않으면 조회에 실패한다.")
    @Test
    fun findMemberOrElseThrowWithoutMember() {
        // given
        val memberId = 100L


        // when & then
        assertThatThrownBy {
            memberQueryService.findMemberOrElseThrow(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }


    @DisplayName("소셜 로그인 회원이 존재하면 해당 회원을 조회한다.")
    @Test
    fun findMemberFromProviderId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberQueryService.findMemberFromProviderId(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)
    }

    @DisplayName("소셜 로그인 회원이 존재하지 않으면 해당 회원 조회에 실패한다.")
    @Test
    fun findMemberFromProviderIdWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberQueryService.findMemberFromProviderId(providerId)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("상대 회원의 프로필을 조회한다.")
    @Test
    fun findMemberDetail() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, otherMember.id!!)

        // when
        val response = memberQueryService.findMemberDetail(me.id!!, otherMember.id!!)

        // then
        assertThat(response.profile).isNotNull
        response.profile.let {
            assertThat(it.memberId).isEqualTo(otherMember.id)
            assertThat(it.nickname).isEqualTo(request.nickname)
            assertThat(it.gender).isEqualTo(request.gender)
            assertThat(it.height).isEqualTo(request.height)
            assertThat(it.weight).isEqualTo(request.weight)
            assertThat(it.intro).isEqualTo(request.intro)
            assertThat(it.age).isEqualTo(Age.fromBirthDay(request.birthday))
            assertThat(it.workoutExperience).isEqualTo(request.workoutExperience)
            assertThat(it.workoutStyle).isEqualTo(request.workoutStyle)
            assertThat(it.workoutGoal).isEqualTo(request.workoutGoal)
            assertThat(it.score).isEqualTo(otherMember.score!!.toInt())

            assertThat(it.siDo).isEqualTo(request.address.siDo)
            assertThat(it.siGunGu).isEqualTo(request.address.siGunGu)
            assertThat(it.eupMyeonDong).isEqualTo(request.address.eupMyeonDong)

            assertThat(it.workoutTimeNames).containsExactlyInAnyOrderElementsOf(request.workoutTimes)
            assertThat(it.workoutImageUrls).containsExactlyInAnyOrderElementsOf(request.workoutImageUrls)

            assertThat(it.profileImageUrl).isEqualTo(request.profileImageUrl)
        }
        response.workoutPartner.let {
            assertThat(it.status).isEqualTo(ProfileWorkoutPartnerStatus.NONE)
            assertThat(it.workoutPartnerRequestId).isEqualTo(null)
            assertThat(it.chatRoomId).isEqualTo(null)
        }
    }


    @DisplayName("인근 회원을 페이징 형태로 조회한다.")
    @Test
    fun findRandomMemberWithinLocal() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val member4 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member4)
        val outAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress1
        )
        oAuth2Service.signup(signupRequest4, member4.id!!)

        val condition = MemberLocalCondition(
            page = 1,
            size = 10
        )

        given(redisClient.get(any()))
            .willReturn(null)

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = 123L
        )

        // then
        assertThat(response.number).isEqualTo(0)
        assertThat(response.size).isEqualTo(10)
        assertThat(response.numberOfElements).isEqualTo(3)
        assertThat(response.hasNext()).isFalse()


        val content = response.content
        assertThat(content)
            .extracting("memberId", "nickname", "workoutExperience", "workoutStyle", "workoutGoal", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(
                    member3.id!!,
                    signupRequest3.nickname,
                    signupRequest3.workoutExperience,
                    signupRequest3.workoutStyle,
                    signupRequest3.workoutGoal,
                    signupRequest3.profileImageUrl,
                ),
                tuple(
                    member2.id!!,
                    signupRequest2.nickname,
                    signupRequest2.workoutExperience,
                    signupRequest2.workoutStyle,
                    signupRequest2.workoutGoal,
                    signupRequest2.profileImageUrl,
                ),
                tuple(
                    member1.id!!,
                    signupRequest1.nickname,
                    signupRequest1.workoutExperience,
                    signupRequest1.workoutStyle,
                    signupRequest1.workoutGoal,
                    signupRequest1.profileImageUrl,
                ),
            )
    }

    @DisplayName("현재 회원의 인근 회원 조회 시, 상대방과의 운동 파트너 요청 기록을 조회한다.")
    @Test
    fun findMemberWithinLocalExistsWorkoutPartnerRequest() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        println(member3.id!!)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest(
            fromMember = baseMember,
            toMember = member1,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest2 = WorkoutPartnerRequest(
            fromMember = member2,
            toMember = baseMember,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest3 = WorkoutPartnerRequest(
            fromMember = baseMember,
            toMember = member3,
            status = WorkoutPartnerRequestStatus.ACCEPT,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = baseMember,
            memberTwo = member3,
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(type = ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatParticipant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = baseMember,
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member3,
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1
        )

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(baseMember.id!!, condition, 123L)

        // then
        assertThat(response)
            .extracting(
                "memberId",
                "lastWorkoutPartnerRequest.workoutPartnerRequestId",
                "lastWorkoutPartnerRequest.status",
//                "lastWorkoutPartnerRequest.isSentByMe",
                "lastWorkoutPartnerRequest.chatRoomId",
            )
            .containsExactlyInAnyOrder(
                tuple(
                    member3.id!!,
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status!!,
//                    true,
                    chatRoom.id!!
                ),
                tuple(
                    member2.id!!,
                    workoutPartnerRequest2.id!!,
                    workoutPartnerRequest2.status!!,
//                    false,
                    null
                ),
                tuple(
                    member1.id!!,
                    workoutPartnerRequest1.id!!,
                    workoutPartnerRequest1.status!!,
//                    true,
                    null
                ),
            )
    }

    @DisplayName("현재 회원의 인근 회원 조회 시, 상대방과의 운동 파트너 요청 기록은 24시간 이내의 것만 조회한다. ACCEPT는 옛날 것도 조회")
    @Test
    fun findMemberWithinLocalExistsWorkoutPartnerRequestWithin24Hour() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val workoutPartnerRequest1 = WorkoutPartnerRequest(
            fromMember = baseMember,
            toMember = member1,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime.minusHours(24).minusSeconds(1),
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest3 = WorkoutPartnerRequest(
            fromMember = baseMember,
            toMember = member3,
            status = WorkoutPartnerRequestStatus.ACCEPT,
            requestedAt = time.nowLocalDateTime.minusHours(24).minusSeconds(1),
            content = WorkoutPartnerRequestContent.BURN,
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = baseMember,
            memberTwo = member3,
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(type = ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatParticipant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = baseMember,
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member3,
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1
        )

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(baseMember.id!!, condition, 123L)

        // then
        assertThat(response)
            .extracting(
                "memberId",
                "lastWorkoutPartnerRequest.workoutPartnerRequestId",
                "lastWorkoutPartnerRequest.status",
//                "lastWorkoutPartnerRequest.isSentByMe",
                "lastWorkoutPartnerRequest.chatRoomId",
            )
            .containsExactlyInAnyOrder(
                tuple(
                    member3.id!!,
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status!!,
//                    true,
                    chatRoom.id!!
                ),
                tuple(
                    member2.id!!,
                    null,
                    null,
//                    null,
                    null
                ),
                tuple(
                    member1.id!!,
                    null,
                    null,
//                    null,
                    null
                ),
            )
    }

    @DisplayName("인근 회원 조회 시, seed 기반의 랜덤 셔플링을 해서 조회한다.")
    @Test
    fun findRandomMemberWithinLocalRandom() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            radiusKm = 10
        )

        val seed = 123L

        given(redisClient.get(any()))
            .willReturn(null)

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val memberMaxId = member3.id!!
        val randomMemberId = randomCustom.nextLong(seed, 1, memberMaxId + 1)
        val members = listOf(member1, member2, member3)

        val left = members.filter { it.id!! >= randomMemberId }.sortedBy { it.id }
        val right = members.filter { it.id!! in 1..<randomMemberId }.sortedBy { it.id }

        val responseMember = left + right

        val shuffledResponseMember = randomCustom.shuffled(seed, responseMember).toMutableList()

        assertThat(response).hasSize(3)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledResponseMember.map { it.id })
    }

    @DisplayName("인근 회원 조회 시, 전체 조회 개수 100개를 미달하면 서울 인원으로 추가 조회한다..")
    @Test
    fun findRandomMemberWithinLocalAddSeoul() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val member4 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member4)
        val outAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress1
        )
        oAuth2Service.signup(signupRequest4, member4.id!!)

        val member5 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member5)
        val outAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick1",
            address = outAddress2
        )
        oAuth2Service.signup(signupRequest5, member5.id!!)


        val member6 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member6)
        val outAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest6 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress3
        )
        oAuth2Service.signup(signupRequest6, member6.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            radiusKm = 10
        )

        given(redisClient.get(any()))
            .willReturn(null)

        val seed = 123L

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val memberMaxId = member6.id!!
        val randomMemberId = randomCustom.nextLong(seed, 1, memberMaxId + 1)
        val members = listOf(member1, member2, member3)

        val left = members.filter { it.id!! >= randomMemberId }.sortedBy { it.id }
        val right = members.filter { it.id!! in 1..<randomMemberId }.sortedBy { it.id }

        val responseMember = left + right

        val shuffledResponseMember = randomCustom.shuffled(seed, responseMember).toMutableList()

        val shuffledSeoulMembers = randomCustom.shuffled(seed, listOf(member4, member6))

        shuffledResponseMember.addAll(shuffledSeoulMembers)

        assertThat(response).hasSize(5)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledResponseMember.map { it.id })
    }

    @DisplayName("인근 회원 조회 시, 캐시된 회원 랜덤 id가 있으면 사용한다.")
    @Test
    fun findRandomMemberWithinLocalByCacheMemberRandomId() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        addressRepository.save(address)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            radiusKm = 10
        )

        given(redisClient.get(any()))
            .willReturn(member1.id!!.toString())

        val seed = 123L

        // when
        val response = memberQueryService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val members = listOf(member1, member2, member3)
        val shuffledMembers = randomCustom.shuffled(seed, members)

        assertThat(response).hasSize(3)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledMembers.map { it.id })
    }

    @DisplayName("추천 핏버디 - 운동 경력/스타일/목표가 2개 이상 맞는 회원을 조회한다.")
    @Test
    fun findRandomMemberWithinRecommendation() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)

        val matchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.TENSION,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2")
        )
        oAuth2Service.signup(signupRequest3, matchMember2.id!!)

        val matchMember3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember3)
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2")
        )
        oAuth2Service.signup(signupRequest4, matchMember3.id!!)

        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val address1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "fullAddress"
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            address = address1
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)

        val seed = 123L

        // when
        val response = memberQueryService.findRandomMemberWithinRecommendation(
            memberId = me.id!!,
            size = 10,
            seed = seed
        )

        // then
        assertThat(response).hasSize(3)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactlyInAnyOrder(
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
                tuple(matchMember2.id!!, "update2", "updateMember2Workout1"),
                tuple(matchMember3.id!!, "update3", "updateMember3Workout1"),
            )
    }

    @DisplayName("추천 핏버디 조회 시, 운동 파트너 요청 여부를 조회한다.")
    @Test
    fun findRandomMemberWithinRecommendationExistsWorkoutPartnerRequest() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)

        val matchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.TENSION,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2")
        )
        oAuth2Service.signup(signupRequest3, matchMember2.id!!)

        val matchMember3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember3)
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2")
        )
        oAuth2Service.signup(signupRequest4, matchMember3.id!!)


        val workoutPartnerRequest1 = WorkoutPartnerRequest(
            fromMember = me,
            toMember = matchMember1,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest2 = WorkoutPartnerRequest(
            fromMember = matchMember2,
            toMember = me,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        val workoutPartnerRequest3 = WorkoutPartnerRequest(
            fromMember = me,
            toMember = matchMember3,
            status = WorkoutPartnerRequestStatus.ACCEPT,
            requestedAt = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN,
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = me,
            memberTwo = matchMember3,
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
            member = matchMember3,
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val seed = 123L

        // when
        val response = memberQueryService.findRandomMemberWithinRecommendation(
            memberId = me.id!!,
            size = 10,
            seed = seed
        )

        // then
        assertThat(response)
            .extracting(
                "memberId",
                "lastWorkoutPartnerRequest.workoutPartnerRequestId",
                "lastWorkoutPartnerRequest.status",
//                "lastWorkoutPartnerRequest.isSentByMe",
                "lastWorkoutPartnerRequest.chatRoomId",
            )
            .contains(
                tuple(
                    matchMember3.id!!,
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status!!,
//                    true,
                    chatRoom.id!!
                ),
                tuple(
                    matchMember2.id!!,
                    workoutPartnerRequest2.id!!,
                    workoutPartnerRequest2.status!!,
//                    false,
                    null
                ),
                tuple(
                    matchMember1.id!!,
                    workoutPartnerRequest1.id!!,
                    workoutPartnerRequest1.status!!,
//                    true,
                    null
                ),
            )
    }


    @DisplayName("추천 핏버디 조회 시, 조회 개수가 못미치면 아무 서울 인원도 추가 조회한다.")
    @Test
    fun findRandomMemberWithinRecommendationNotEnoughMember() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)

        val matchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.TENSION,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2")
        )
        oAuth2Service.signup(signupRequest3, matchMember2.id!!)

        val matchMember3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember3)
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2")
        )
        oAuth2Service.signup(signupRequest4, matchMember3.id!!)

        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val address1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "fullAddress"
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update4",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            address = address1,
            workoutImageUrls = listOf("updateMember4Workout1", "updateMember4Workout2")
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)

        val seed = 123L

        // when
        val response = memberQueryService.findRandomMemberWithinRecommendation(
            memberId = me.id!!,
            size = 10,
            seed = seed
        )

        // then
        assertThat(response).hasSize(4)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactlyInAnyOrder(
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
                tuple(matchMember2.id!!, "update2", "updateMember2Workout1"),
                tuple(matchMember3.id!!, "update3", "updateMember3Workout1"),
                tuple(notMatchMember1.id!!, "update4", "updateMember4Workout1"),
            )
    }

    @DisplayName("삭제된 회원을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNotNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        member.delete(time.nowLocalDateTime)

        memberRepository.save(member)

        // when
        val findMember = memberQueryService.findDeletedMemberFrom(member.id!!)

        // then
        assertThat(findMember!!.id!!).isEqualTo(member.id!!)
        assertThat(findMember.deletedAt).isNotNull()
    }

    @DisplayName("특정일에 계정을 생성한 전체 회원 개수를 조회한다.")
    @Test
    fun countMemberFromCreatedAtDate() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = null
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        // when
        val count = memberQueryService.countMemberFromCreatedAtDate(time.nowLocalDate)

        // then
        assertThat(count).isEqualTo(2)
    }


    @DisplayName("특정일 기준으로 회원가입을 하지 않은 회원을 조회한다.")
    @Test
    fun countNotSignupMemberFromCreatedAtDate() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            isSignup = true
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = null,
            isSignup = false
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        // when
        val count = memberQueryService.countNotSignupMemberFromCreatedAtDate(time.nowLocalDate)

        // then
        assertThat(count).isEqualTo(1)
    }

    @DisplayName("회원 닉네임이 있는지 확인한다.")
    @Test
    fun existsMemberNickname() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname"
        )

        oAuth2Service.signup(
            request = request,
            memberId = member.id!!
        )

        // when
        val response = memberQueryService.existsMemberNickname(
            nickname = "nickname"
        )

        // then
        assertThat(response).isEqualTo(true)
    }

    @DisplayName("없는 닉네임이라면 false를 반환한다.")
    @Test
    fun existsMemberNicknameNotExistsNickname() {
        // given

        // when
        val response = memberQueryService.existsMemberNickname(
            nickname = "nickname"
        )

        // then
        assertThat(response).isEqualTo(false)
    }

}