package kr.co.fitview.api.app.domain.favorite.controller

import kr.co.fitview.api.app.domain.favorite.condition.FavoriteMemberCondition
import kr.co.fitview.api.app.domain.favorite.dto.response.FavoriteMemberResponse
import kr.co.fitview.api.app.domain.favorite.service.FavoriteQueryService
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
    private val favoriteQueryService: FavoriteQueryService,
    private val securityUtil: SecurityUtil,
) {


    @GetMapping("/members")
    fun favoriteMember(
        @ModelAttribute
        condition: FavoriteMemberCondition,
    ): ResponseEntity<ApiResponse<SuccessCursorPagedResponse<FavoriteMemberResponse>>> {

        val response = favoriteQueryService.findFavoriteMembers(securityUtil.getMemberId(), condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
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