package kr.co.fitview.api.app.domain.favorite.controller

import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.service.FavoriteService
import kr.co.fitview.api.app.domain.member.dto.response.LastWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/v1/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService,
    private val securityUtil: SecurityUtil,
) {


    @GetMapping("/members")
    fun favoriteMember(
        @ModelAttribute
        condition: FavoriteMemberCondition,
    ): ResponseEntity<ApiResponse<SuccessCursorPagedResponse<FavoriteMemberResponse>>> {

        // 0. 사용할 이미지 리스트 정의
        val profileImageUrls = listOf(
            "https://static-dev.fitview.co.kr/member/profile/05fb391f-217b-433a-a160-1269f99a1f17",
            "https://static-dev.fitview.co.kr/member/profile/0419cb6d-3df1-4c15-bc3b-b95de5d0d739",
            "https://static-dev.fitview.co.kr/member/profile/5c7cff75-46e1-47e3-a738-2c2c1279559d",
            "https://static-dev.fitview.co.kr/member/profile/a5c96d4f-fc60-4909-a3c3-99db79fc6df2",
            "https://static-dev.fitview.co.kr/member/profile/3ada59e8-ac00-4237-8498-f7ce3405d7da",
            "https://static-dev.fitview.co.kr/member/profile/feb68e25-4f13-4bf6-8fe0-0334b66c37a5",
            "https://static-dev.fitview.co.kr/member/profile/419e3b60-9d60-4b34-8820-bcd7ade1510f"
        )

        val workoutImageUrls = listOf(
            "https://static-dev.fitview.co.kr/member/workout/9e489fbe-99f4-41b3-8739-ae78c7d06321",
            "https://static-dev.fitview.co.kr/member/workout/86835b04-d97a-4d57-9d4b-15f04a9c6549",
            "https://static-dev.fitview.co.kr/member/workout/4271bcdd-fd7d-4100-9c12-96c4f8abc069",
            "https://static-dev.fitview.co.kr/member/workout/1f1b1c0c-3497-48d7-8aa5-959a16409293",
            "https://static-dev.fitview.co.kr/member/workout/b5ca6537-3c9b-45b4-97a3-dd8a66fd71ec",
            "https://static-dev.fitview.co.kr/member/workout/f5deced7-b54b-488e-8c6e-3cb59656b21d"
        )

        // 1. 100개의 가짜 데이터 생성 (1 ~ 100)
        val random = Random()
        val allData = (1..100).map { i ->
            val id = i.toLong() // favoriteId는 1~100 순차 생성 (나중에 정렬됨)

            // ★ 수정됨: memberId를 1000 ~ 99999 사이의 랜덤값으로 생성 (중구난방)
            val randomMemberId = (1000L..99999L).random()

            val profileImg = profileImageUrls[(i - 1) % profileImageUrls.size]
            val workoutImg = workoutImageUrls[(i - 1) % workoutImageUrls.size]

            // 50% 확률로 lastWorkoutPartnerRequest 생성
            val requestResponse = if (random.nextBoolean()) {
                val status =
                    if (random.nextBoolean()) WorkoutPartnerRequestStatus.PENDING else WorkoutPartnerRequestStatus.ACCEPT
                val chatRoomId = if (status == WorkoutPartnerRequestStatus.ACCEPT && random.nextBoolean()) {
                    id + 1000
                } else {
                    null
                }
                LastWorkoutPartnerRequestResponse(
                    workoutPartnerRequestId = id * 10,
                    status = status,
                    chatRoomId = chatRoomId
                )
            } else {
                null
            }

            FavoriteMemberResponse(
                favoriteId = id,
                memberId = randomMemberId, // <-- 여기가 랜덤값으로 들어갑니다
                nickname = "헬스장고인물_$randomMemberId", // 닉네임도 memberId에 맞춰 변경
                workoutExperience = MemberWorkoutExperience.entries.toTypedArray().random(),
                workoutStyle = MemberWorkoutStyle.entries.toTypedArray().random(),
                workoutGoal = MemberWorkoutGoal.entries.toTypedArray().random(),
                profileImageUrl = profileImg,
                workoutImageUrl = if (random.nextBoolean()) workoutImg else null,
                lastWorkoutPartnerRequest = requestResponse
            )
        }.sortedByDescending { it.favoriteId } // favoriteId 기준으로만 정렬

        // 2. 커서 필터링
        val filteredData = if (condition.cursorFavoriteId == null) {
            allData
        } else {
            allData.filter { it.favoriteId < condition.cursorFavoriteId!! }
        }

        // 3. Slice 로직 구현
        val limit = condition.size
        val dataWithNext = filteredData.take(limit + 1)

        val hasNext = dataWithNext.size > limit
        val content = if (hasNext) dataWithNext.dropLast(1) else dataWithNext
        val pageable = PageRequest.of(0, limit)

        // 4. 응답 생성
        val slice = SliceImpl(content, pageable, hasNext)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = slice,
                idExtractor = { it.favoriteId }
            )
        )
    }


    @PostMapping("/members/{memberId}")
    fun favoriteAdd(
        @PathVariable
        memberId: Long
    ): ResponseEntity<ApiResponse<*>> {
        favoriteService.favoriteAdd(securityUtil.getMemberId(), memberId)

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @DeleteMapping("/members/{memberId}")
    fun favoriteDelete(
        @PathVariable
        memberId: Long
    ): ResponseEntity<ApiResponse<*>> {
        favoriteService.favoriteDelete(securityUtil.getMemberId(), memberId)

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


}