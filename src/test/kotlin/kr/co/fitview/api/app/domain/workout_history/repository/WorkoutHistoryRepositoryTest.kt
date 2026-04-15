package kr.co.fitview.api.app.domain.workout_history.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.review.entity.QReviewReminderLog.reviewReminderLog
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewReminderLog
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewReminderLogRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryRepositoryTest @Autowired constructor(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatMessageRepository : ChatMessageRepository,
    val chatNoticeMessageRepository: ChatNoticeMessageRepository,
    val workoutRequestRepository : WorkoutRequestRepository,
    val reviewReminderLogRepository : ReviewReminderLogRepository,
    val reviewRepository : ReviewRepository,
    val oAuth2Service : OAuth2Service,
    val workoutHistoryQueryService : WorkoutHistoryQueryService,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원 간의 운동 이력 여부가 있다.")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)
    }

    @DisplayName("회원 간의 운동 이력 여부가 없다")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNullIsFalse() {
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)


        // when
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(false)
    }

    @DisplayName("회원 간의 운동 이력 여부 확인 시, 회원 순서가 뒤바뀌면 여부 확인이 안된다.")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNullReverse() {
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(false)
    }

    @DisplayName("회원 간의 운동 여부 확인 시, 회원의 순서를 오름차순으로 자동 정렬해서 조회한다.")
    @Test
    fun findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val existsWorkoutHistory = workoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)

    }

    @DisplayName("운동 이력 조회")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val findWorkoutHistory = workoutHistoryRepository.findByIdAndDeletedAtIsNull(workoutHistory.id!!)

        // then
        assertThat(findWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("각 채팅방의 운동 완료 여부를 조회한다.")
    @Test
    fun findWorkoutHistoryBy() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        val chatRoom2 = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)
        chatRoomRepository.save(chatRoom2)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val chatRoomIds = listOf(chatRoom.id!!, chatRoom2.id!!)

        // when
        val findWorkoutHistories = workoutHistoryRepository.findWorkoutHistoryBy(chatRoomIds)

        // then
        assertThat(findWorkoutHistories)
            .extracting("chatRoomId", "isCompleteWorkoutHistory")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, true),
                tuple(chatRoom2.id!!, false),
            )
    }

    @DisplayName("24시간동안 리뷰 작성이 없는 운동 완료 이력을 조회한다.")
    @Test
    fun findAllWorkoutHistoryExceed24HoursWithoutReview() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(23).minusMinutes(59)
        )
        workoutHistoryRepository.save(workoutHistory2)

        val chatMessageFromNotice1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice1)

        val chatNoticeMessage1 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice1,
            workoutHistory = workoutHistory
        )
        chatNoticeMessageRepository.save(chatNoticeMessage1)

        val chatMessageFromNotice2 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice2)

        val chatNoticeMessage2 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice2,
            workoutHistory = workoutHistory2
        )
        chatNoticeMessageRepository.save(chatNoticeMessage2)

        // when
        val findWorkoutHistories = workoutHistoryRepository.findAllWorkoutHistoryExceed24HoursWithoutReview()


        // then
        assertThat(findWorkoutHistories).hasSize(1)
        assertThat(findWorkoutHistories[0].workoutHistory)
            .extracting("chatRoom", "workoutRequest", "memberOne", "memberTwo", "completedAt")
            .contains(chatRoom, workoutRequest, me, other, time.nowLocalDateTime.minusHours(24))

    }

    @DisplayName("24시간동안 리뷰 이력이 없는 운동 이력 조회 시, 이미 리뷰 이력이 있다면 조회하지 않는다.")
    @Test
    fun findAllWorkoutHistoryExceed24HoursWithoutReviewExistsReview() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory2)

        val chatMessageFromNotice1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice1)

        val chatNoticeMessage1 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice1,
            workoutHistory = workoutHistory
        )
        chatNoticeMessageRepository.save(chatNoticeMessage1)

        val chatMessageFromNotice2 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice2)

        val chatNoticeMessage2 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice2,
            workoutHistory = workoutHistory2
        )
        chatNoticeMessageRepository.save(chatNoticeMessage2)

        val review = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = workoutHistory2,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 1.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review)

        // when
        val findWorkoutHistoryAndChatMessage = workoutHistoryRepository.findAllWorkoutHistoryExceed24HoursWithoutReview()

        // then
        assertThat(findWorkoutHistoryAndChatMessage).hasSize(1)
        assertThat(findWorkoutHistoryAndChatMessage[0].workoutHistory)
            .extracting("chatRoom", "workoutRequest", "memberOne", "memberTwo", "completedAt")
            .contains(chatRoom, workoutRequest, me, other, time.nowLocalDateTime.minusHours(24))
    }

    @DisplayName("24시간동안 리뷰 이력이 없는 운동 이력 조회 시, 이미 리뷰 권유 이력이 있다면 조회하지 않는다.")
    @Test
    fun findAllWorkoutHistoryExceed24HoursWithoutReviewReviewReminder() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory2)

        val chatMessageFromNotice1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice1)

        val chatNoticeMessage1 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice1,
            workoutHistory = workoutHistory
        )
        chatNoticeMessageRepository.save(chatNoticeMessage1)

        val chatMessageFromNotice2 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice2)

        val chatNoticeMessage2 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice2,
            workoutHistory = workoutHistory2
        )
        chatNoticeMessageRepository.save(chatNoticeMessage2)

        val reviewReminderLog = ReviewReminderLog(
            workoutHistory = workoutHistory,
            type = ReviewReminderLogType.REVIEW_24H,
            sentAt = time.nowLocalDateTime
        )
        reviewReminderLogRepository.save(reviewReminderLog)

        // when
        val findWorkoutHistoryAndChatMessages = workoutHistoryRepository.findAllWorkoutHistoryExceed24HoursWithoutReview()

        // then
        assertThat(findWorkoutHistoryAndChatMessages).hasSize(1)
        assertThat(findWorkoutHistoryAndChatMessages[0].workoutHistory)
            .extracting("id", "chatRoom", "workoutRequest", "memberOne", "memberTwo", "completedAt")
            .contains(workoutHistory2.id!!, chatRoom, workoutRequest2, me, other, time.nowLocalDateTime.minusHours(24))
        assertThat(findWorkoutHistoryAndChatMessages[0].chatMessage)
            .extracting("id", "chatRoom", "type")
            .contains(chatMessageFromNotice2.id!!, chatRoom, ChatMessageType.NOTICE)
    }

    @DisplayName("운동 이력 최근 이력을 조회한다.")
    @Test
    fun findAllWorkoutHistoryBy() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val oAuth2Request1 = TestDataFactory.oAuth2SignupRequest(nickname = "me")
        val oAuth2Request2 = TestDataFactory.oAuth2SignupRequest(nickname = "other")
        oAuth2Service.signup(memberId = me.id!!, request = oAuth2Request1)
        oAuth2Service.signup(memberId = other.id!!, request = oAuth2Request2)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val histories = (1..6).map { i ->
            val chatMessage = ChatMessage(
                member = me,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                content = "content $i",
                sentAt = time.nowLocalDateTime
            ).also { chatMessageRepository.save(it) }

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage,
                fromMember = me,
                toMember = other,
                location = "location $i",
                scheduledAt = time.nowLocalDateTime.plusDays(1),
                requestedAt = time.nowLocalDateTime
            ).also { workoutRequestRepository.save(it) }

            WorkoutHistory(
                chatRoom = chatRoom,
                workoutRequest = workoutRequest,
                memberOne = me,
                memberTwo = other,
                completedAt = time.nowLocalDateTime.minusHours((7 - i).toLong())
            ).also { workoutHistoryRepository.save(it) }
        }

        val review = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = histories[5],
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review)

        // when
        val response = workoutHistoryRepository.findAllWorkoutHistoryBy(
            memberId = me.id!!,
            startDate = time.nowLocalDate.minusYears(1),
            limit = 5
        )

        // then
        assertThat(response).hasSize(5)

        assertThat(response)
            .extracting(
                "workoutHistoryId",
                "nickname",
                "completedAt",
                "isReviewed"
            )
            .containsExactly(
                tuple(histories[5].id!!, "other", histories[5].completedAt, true),
                tuple(histories[4].id!!, "other", histories[4].completedAt, false),
                tuple(histories[3].id!!, "other", histories[3].completedAt, false),
                tuple(histories[2].id!!, "other", histories[2].completedAt, false),
                tuple(histories[1].id!!, "other", histories[1].completedAt, false)
            )
    }

    @DisplayName("운동 이력 조회 시, limit 개수만큼만 조회된다.")
    @Test
    fun findAllWorkoutHistoryByLimit() {
        val me = Member(email = "email", password = "password", role = Role.USER)
        val other = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(me)
        memberRepository.save(other)

        oAuth2Service.signup(memberId = me.id!!, request = TestDataFactory.oAuth2SignupRequest(nickname = "me"))
        oAuth2Service.signup(memberId = other.id!!, request = TestDataFactory.oAuth2SignupRequest(nickname = "other"))

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val histories = (1..6).map { i ->
            val chatMessage = ChatMessage(
                member = me, chatRoom = chatRoom, type = ChatMessageType.WORKOUT_REQUEST, content = "content $i", sentAt = time.nowLocalDateTime
            ).also { chatMessageRepository.save(it) }

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage, fromMember = me, toMember = other, location = "location $i", scheduledAt = time.nowLocalDateTime.plusDays(1), requestedAt = time.nowLocalDateTime
            ).also { workoutRequestRepository.save(it) }

            WorkoutHistory(
                chatRoom = chatRoom, workoutRequest = workoutRequest, memberOne = me, memberTwo = other,
                completedAt = time.nowLocalDateTime.minusHours((7 - i).toLong())
            ).also { workoutHistoryRepository.save(it) }
        }

        val review = Review(
            fromMember = me, toMember = other, workoutHistory = histories[5], isPrivate = false, type = ReviewType.GOOD, score = 2.0, content = "content", postedAt = time.nowLocalDateTime
        ).also { reviewRepository.save(it) }

        val response = workoutHistoryRepository.findAllWorkoutHistoryBy(
            memberId = me.id!!,
            startDate = time.nowLocalDate.minusYears(1),
            limit = 2
        )

        assertThat(response).hasSize(2)

        assertThat(response)
            .extracting("workoutHistoryId", "nickname", "completedAt", "isReviewed")
            .containsExactly(
                tuple(histories[5].id!!, "other", histories[5].completedAt, true),
                tuple(histories[4].id!!, "other", histories[4].completedAt, false)
            )
    }

    @DisplayName("운동 이력 조회 시, startDate 이후의 데이터만 조회된다.")
    @Test
    fun findAllWorkoutHistoryByStartDate() {
        val me = Member(email = "email", password = "password", role = Role.USER)
        val other = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(me)
        memberRepository.save(other)

        oAuth2Service.signup(memberId = me.id!!, request = TestDataFactory.oAuth2SignupRequest(nickname = "me"))
        oAuth2Service.signup(memberId = other.id!!, request = TestDataFactory.oAuth2SignupRequest(nickname = "other"))

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val histories = (1..6).map { i ->
            val chatMessage = ChatMessage(
                member = me, chatRoom = chatRoom, type = ChatMessageType.WORKOUT_REQUEST, content = "content $i", sentAt = time.nowLocalDateTime
            ).also { chatMessageRepository.save(it) }

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage, fromMember = me, toMember = other, location = "location $i", scheduledAt = time.nowLocalDateTime.plusDays(1), requestedAt = time.nowLocalDateTime
            ).also { workoutRequestRepository.save(it) }

            WorkoutHistory(
                chatRoom = chatRoom, workoutRequest = workoutRequest, memberOne = me, memberTwo = other,
                completedAt = time.nowLocalDateTime.minusDays((7 - i).toLong())
            ).also { workoutHistoryRepository.save(it) }
        }

        val review = Review(
            fromMember = me, toMember = other, workoutHistory = histories[5], isPrivate = false, type = ReviewType.GOOD, score = 2.0, content = "content", postedAt = time.nowLocalDateTime
        ).also { reviewRepository.save(it) }

        val response = workoutHistoryRepository.findAllWorkoutHistoryBy(
            memberId = me.id!!,
            startDate = time.nowLocalDate.minusDays(3),
            limit = 5
        )

        assertThat(response).hasSize(3)

        assertThat(response)
            .extracting("workoutHistoryId", "nickname", "completedAt", "isReviewed")
            .containsExactly(
                tuple(histories[5].id!!, "other", histories[5].completedAt, true),
                tuple(histories[4].id!!, "other", histories[4].completedAt, false),
                tuple(histories[3].id!!, "other", histories[3].completedAt, false)
            )
    }
}