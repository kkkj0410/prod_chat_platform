package kr.co.fitview.api.app.domain.banner.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class BannerQueryServiceTest @Autowired constructor(
    val bannerQueryService : BannerQueryService,
    val bannerRepository : BannerRepository,
    val imageRepository: ImageRepository,
    val memberRepository: MemberRepository,
    val reviewRepository: ReviewRepository,
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val chatRoomRepository: ChatRoomRepository,
    val chatMessageRepository: ChatMessageRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport() {

    @DisplayName("배너를 프록시로 조회한다.")
    @Test
    fun findBannerReferenceFrom() {
        // given
        val image = Image(url = "url")
        imageRepository.save(image)

        val banner = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        em.flush()
        em.clear()

        // when
        val findBanner = bannerQueryService.findBannerReferenceFrom(banner.id!!)

        // then
        assertThat(findBanner).isInstanceOf(HibernateProxy::class.java)
        assertThat(findBanner)
            .extracting("bannerType")
            .isEqualTo(BannerType.APP_FEEDBACK)
    }

    @DisplayName("스탬프가 0개일 때 앱 피드백과 운동 리워드 배너가 모두 조회된다.")
    @Test
    fun findActiveBannersWithZeroStamp() {
        // given
        val member = Member(
            email = "email@email.com",
            password = "password",
            nickname = "nickname",
            role = Role.USER
        )
        memberRepository.save(member)

        val image = Image(url = "url")
        imageRepository.save(image)

        val banner1 = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner1)

        val banner2 = Banner(
            image = null,
            bannerType = BannerType.WORKOUT_REWARD,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)

        // when
        val findBanners = bannerQueryService.findActiveBanners(member.id!!)

        // then
        assertThat(findBanners).hasSize(2)
        assertThat(findBanners).extracting("type")
            .containsExactlyInAnyOrder(BannerType.APP_FEEDBACK, BannerType.WORKOUT_REWARD)
    }

    @DisplayName("스탬프가 1개 이상일 때 운동 리워드 배너는 조회되지 않는다.")
    @Test
    fun findActiveBannersWithExistingStamp() {
        // given
        val member = Member(
            email = "member@email.com",
            password = "password",
            nickname = "member",
            role = Role.USER
        )
        memberRepository.save(member)

        val partner = Member(
            email = "partner@email.com",
            password = "password",
            nickname = "partner",
            role = Role.USER
        )
        memberRepository.save(partner)
        
        val chatRoom = chatRoomRepository.save(
            ChatRoom(
                type = ChatRoomType.PRIVATE
            )
        )

        val chatMessage = chatMessageRepository.save(
            ChatMessage(
                member = member,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                sentAt = time.nowLocalDateTime
            )
        )

        val workoutRequest = workoutRequestRepository.save(
            WorkoutRequest(
                chatMessage = chatMessage,
                fromMember = member,
                toMember = partner,
                status = WorkoutRequestStatus.PENDING,
                location = "location",
                scheduledAt = time.nowLocalDateTime,
                requestedAt = time.nowLocalDateTime
            )
        )

        val workoutHistory = workoutHistoryRepository.save(
            WorkoutHistory.of(chatRoom, workoutRequest, member, partner, time.nowLocalDateTime)
        )
        reviewRepository.save(
            Review(
                fromMember = member,
                toMember = partner,
                workoutHistory = workoutHistory,
                type = ReviewType.GOOD,
                score = 5.0,
                isPrivate = false,
                postedAt = time.nowLocalDateTime
            )
        )

        val image = Image(url = "url")
        imageRepository.save(image)

        val banner1 = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner1)

        val banner2 = Banner(
            image = null,
            bannerType = BannerType.WORKOUT_REWARD,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)

        // when
        val findBanners = bannerQueryService.findActiveBanners(member.id!!)

        // then
        assertThat(findBanners).hasSize(1)
        assertThat(findBanners[0].type).isEqualTo(BannerType.APP_FEEDBACK)
    }

    @DisplayName("앱 피드백 유형의 배너 조회 시 imageUrl과 svgImageUrl이 포함된다.")
    @Test
    fun findActiveBannersAppFeedbackResponseFields() {
        // given
        val member = Member(
            email = "email@email.com",
            password = "password",
            nickname = "nickname",
            role = Role.USER
        )
        memberRepository.save(member)

        val image = Image(url = "https://png-image-url.com")
        imageRepository.save(image)

        val banner = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        // when
        val findBanners = bannerQueryService.findActiveBanners(member.id!!)

        // then
        assertThat(findBanners).hasSize(1)
        val response = findBanners[0] as BannerActiveResponse.AppFeedback
        assertThat(response.type).isEqualTo(BannerType.APP_FEEDBACK)
        assertThat(response.imageUrl).isEqualTo("https://png-image-url.com")
        assertThat(response.svgImageUrl).isNotNull()
    }
}