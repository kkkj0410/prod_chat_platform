package kr.co.fitview.api.app.domain.workout_history.service

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewReminderLog
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewReminderLogRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.dto.response.enums.WorkoutHistoryReviewStatus
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime

class WorkoutHistoryQueryServiceTest @Autowired constructor(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val workoutHistoryQueryService : WorkoutHistoryQueryService,
    val chatRoomRepository : ChatRoomRepository,
    val chatMessageRepository : ChatMessageRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val reviewRepository : ReviewRepository,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("운동 이력 조회")
    @Test
    fun findWorkoutHistoryFrom() {
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
        val findWorkoutHistory = workoutHistoryQueryService.findWorkoutHistoryFrom(workoutHistory.id!!)

        // then
        assertThat(findWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("각 채팅방의 운동 완료 여부를 조회한다.")
    @Test
    fun findAllWorkoutHistoryFrom() {
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
        val findWorkoutHistories = workoutHistoryQueryService.findAllWorkoutHistoryFrom(chatRoomIds)

        // then
        assertThat(findWorkoutHistories)
            .extracting("chatRoomId", "isCompleteWorkoutHistory")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, true),
                tuple(chatRoom2.id!!, false),
            )
    }

    @DisplayName("리뷰 상태 조회 시, 리뷰가 없으면 리뷰 작성 가능 상태를 나타낸다.")
    @Test
    fun findWorkoutHistoryReviewStatusWritable() {
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

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val response = workoutHistoryQueryService.findWorkoutHistoryReviewStatus(me.id!!, workoutHistory.id!!)

        // then
        assertThat(response.status).isEqualTo(WorkoutHistoryReviewStatus.WRITABLE)
    }


    @DisplayName("리뷰 상태 조회 시, 리뷰가 있으면 리뷰 작성 불가 상태를 나타낸다.")
    @Test
    fun findWorkoutHistoryReviewStatusWritten() {
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

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val review = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )

        reviewRepository.save(review)

        // when
        val response = workoutHistoryQueryService.findWorkoutHistoryReviewStatus(me.id!!, workoutHistory.id!!)

        // then
        assertThat(response.status).isEqualTo(WorkoutHistoryReviewStatus.WRITTEN)
    }

    @DisplayName("리뷰 상태 조회 시, 운동 완료가 3일이 지나면 리뷰 작성 불가 상태를 나타낸다.")
    @Test
    fun findWorkoutHistoryReviewStatusExpired() {
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

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusDays(3).minusSeconds(1)
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val response = workoutHistoryQueryService.findWorkoutHistoryReviewStatus(me.id!!, workoutHistory.id!!)

        // then
        assertThat(response.status).isEqualTo(WorkoutHistoryReviewStatus.EXPIRED)
    }

    @DisplayName("24시간이 지나도 리뷰 작성이 없는 운동 완료 이력을 조회한다.")
//    @Test
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


        // when
        val findWorkoutHistories = workoutHistoryQueryService.findAllWorkoutHistoryExceed24HoursWithoutReview()


        // then
        assertThat(findWorkoutHistories).hasSize(1)
        assertThat(findWorkoutHistories[0])
            .extracting("chatRoom", "workoutRequest", "memberOne", "memberTwo", "completedAt")
            .contains(chatRoom, workoutRequest, me, other, time.nowLocalDateTime.minusHours(24))

    }

}